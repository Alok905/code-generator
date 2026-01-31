package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.alok.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.alok.projects.lovable_clone.dto.subscription.PortalResponse;
import com.alok.projects.lovable_clone.entity.Plan;
import com.alok.projects.lovable_clone.entity.User;
import com.alok.projects.lovable_clone.enums.SubscriptionStatus;
import com.alok.projects.lovable_clone.error.ResourceNotFoundException;
import com.alok.projects.lovable_clone.repository.PlanRepository;
import com.alok.projects.lovable_clone.repository.UserRepository;
import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.PaymentProcessor;
import com.alok.projects.lovable_clone.service.SubscriptionService;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StripePaymentProcessor implements PaymentProcessor {

    private final AuthUtil authUtil;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Value("${client.url}")
    private String frontendUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Plan", request.planId().toString()));
        Long userId = authUtil.getCurrentUserId();
        User user = getUser(userId);

        var paramsBuilder = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(plan.getStripePriceId())
                                .setQuantity(1L)
                                .build()
                )
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSubscriptionData(
                        new SessionCreateParams.SubscriptionData.Builder()
                                .setBillingMode(SessionCreateParams.SubscriptionData.BillingMode.builder()
                                        .setType(SessionCreateParams.SubscriptionData.BillingMode.Type.FLEXIBLE)
                                        .build()
                                )
                                .build()
                )
                .setSuccessUrl(frontendUrl + "?success=true&session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "?cancel.html")
                .putMetadata("user_id", userId.toString())
                .putMetadata("plan_id", plan.getId().toString());
//                .build();

        /** NOTE
         * by default, each interaction will create one subscription in stripe; as it doesn't know our own backend that the user who is interacting is same or different.
         * so, when we set the stripeCustomerId (if the user is already there in the subscription earlier) then it'll take that user's entry only and will not create another entry in the subscription for the user.
         * setting customerEmail will make sure that the user logged in in our system has same username as that inside Stripe.
         */
        try {
            String stripeCustomerId = user.getStripeCustomerId();
            if(stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                paramsBuilder.setCustomerEmail(user.getUsername());
            } else {
                paramsBuilder.setCustomer(stripeCustomerId); // stripe customer id
            }

            Session session = Session.create(paramsBuilder.build()); /// making API call to the Stripe
            return new CheckoutResponse(session.getUrl());
        } catch (StripeException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public PortalResponse openCustomerPortal() {
        return null;
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        log.info("type = {}", type);

        switch (type) {
            ///  one-time, on checkout completed
            case "checkout.session.completed" -> handleCheckoutSessionCompleted((Session) stripeObject, metadata);
            ///  when user cancels, upgrades or any updates   (------- cancel might not trigger this -------)
            case "customer.subscription.updated" -> handleCustomerSubscriptionUpdated((Subscription) stripeObject);
            ///  when subscription ends
            case "customer.subscription.deleted" -> handleCustomerSubscriptionDeleted((Subscription) stripeObject);
            ///  when invoice is paid  (due payment or subscription auto-pay is paid successfully)
            case "invoice.paid" -> handleInvoicePaid((Invoice) stripeObject);
            ///  when invoice is not paid, mark as PAST_DUE
            case "invoice.payment_failed" -> handleInvoicePaymentFailed((Invoice) stripeObject);
            default -> log.debug("Ignoring the event: {}", type);
        }
    }

    private void handleCheckoutSessionCompleted(Session session, Map<String, String> metadata) {
        if(session == null) {
            log.error("session object was null inside handleCheckoutSessionCompleted");
            return;
        }

        /**
         * fetch user_id & plan_id from metadata.
         * it was same as the session object that we had created earlier in the "createCheckoutSessionUrl" method.
         */
        Long userId = Long.parseLong(metadata.get("user_id"));
        Long planId = Long.parseLong( metadata.get("plan_id"));

        /**
         * There are multiple subscriptions in stripe (one user can have many in Stripe, but in our case one user can have one only)
         * One user (in our application) will be treated as one customer (in Stripe).
         */
        String subscriptionId = session.getSubscription();
        String customerId = session.getCustomer();

        /**
         * if the checkout is done first time by the user, then store its "stripeCustomerId" in database.
         * otherwise, each checkout will create a new user even if the user (in our application) is same.
         *  - it is handled in "createCheckoutSessionUrl" method.
         */
        User user = getUser(userId);
        if(user.getStripeCustomerId() == null) {
            user.setStripeCustomerId(customerId);
            userRepository.save(user);
        }

        /**
         * this is not related to stripe, so it should be handled in different service.
         * so that, if we choose any other payment method later, it'll not affect.
         */
        subscriptionService.activateSubscription(userId, planId, subscriptionId, customerId);
    }

    private void handleCustomerSubscriptionUpdated(Subscription subscription){
        if(subscription == null) {
            log.error("subscription object was null inside handleCustomerSubscriptionUpdated");
            return;
        }

        SubscriptionStatus subscriptionStatus = mapStripeStatusToEnum(subscription.getStatus());
        if(subscriptionStatus == null) {
            log.error("Unknown status '{}' for subscription {}",  subscription.getStatus(), subscription.getId());
        }

        /**
         * each subscription contains the start and end time.
         * it'll also be stored in the "Subscription" entity in our database (different service, as it is not part of Stripe).
         *      - user to subscription: one-to-many relation
         */
        SubscriptionItem item = subscription.getItems().getData().get(0);
        Instant periodStart = toInstant(item.getCurrentPeriodStart());
        Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

        /**
         * Plan will contain a value which is "stripePriceId".
         * We'll get the plan id from there (database).
         */
        Long planId = resolvePlanId(item.getPrice());

        subscriptionService.updateSubscription(
                subscription.getId(), subscriptionStatus, periodStart, periodEnd,
                subscription.getCancelAtPeriodEnd(), planId
        );
    }

    /**
     * in the subscription is canceled, then it'll be triggered.
     * delete the subscription from the database (handled in "cancelSubscription" method of subscriptionService (As it is not Stripe related code).
     */
    private void handleCustomerSubscriptionDeleted(Subscription subscription){
        if(subscription == null) {
            log.error("subscription object was null inside handleCustomerSubscriptionDeleted");
            return;
        }

        subscriptionService.cancelSubscription(subscription.getId());
    }


    private void handleInvoicePaid(Invoice invoice) {
        /**
         * invoices are also having Subscription's reference.
         */
        String subscriptionId = extractSubscriptionId(invoice);
        if(subscriptionId == null) return;

        try {
            // Stripe SDK calling Stripe server to get the "subscription" with exchange of "subscription id".
            Subscription subscription = Subscription.retrieve(subscriptionId);
            var item = subscription.getItems().getData().get(0);

            Instant periodStart = toInstant(item.getCurrentPeriodStart());
            Instant periodEnd = toInstant(item.getCurrentPeriodEnd());

            subscriptionService.renewSubscriptionPeriod(
                    subscriptionId,
                    periodStart,
                    periodEnd
            );

        } catch (StripeException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleInvoicePaymentFailed(Invoice invoice){
        /**
         * if the payment failed (auto-debit or manual pay) then it'll be triggered.
         */
        String subscriptionId = extractSubscriptionId(invoice);
        if(subscriptionId == null) return;

        subscriptionService.markSubscriptionPastDue(subscriptionId);
    }

    /// // utility methods ---------------------------------------------------------------------
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", userId.toString()));
    }
    private SubscriptionStatus mapStripeStatusToEnum(String status) {
        return switch (status) {
            case "active" -> SubscriptionStatus.ACTIVE;
            case "trialing" -> SubscriptionStatus.TRIALING;
            case "past_due", "unpaid", "paused", "incomplete_expired" -> SubscriptionStatus.PAST_DUE;
            case "canceled" -> SubscriptionStatus.CANCELED;
            case "incomplete" -> SubscriptionStatus.INCOMPLETE;
            default -> {
                log.warn("Unmapped Stripe status: {}", status);
                yield null;
            }
        };
    }
    private Instant toInstant(Long epoch) {
        return epoch == null ? null : Instant.ofEpochSecond(epoch);
    }

    private Long resolvePlanId(Price price) {
        if(price == null || price.getId() == null) return null;
        return planRepository.findByStripePriceId(price.getId())
                .map(Plan::getId)
                .orElse(null);
    }

    private String extractSubscriptionId(Invoice invoice) {
        var parent = invoice.getParent();
        if(parent == null) return null;

        var subscriptionDetails = parent.getSubscriptionDetails();
        if(subscriptionDetails == null) return null;

        return subscriptionDetails.getSubscription();
    }
}
