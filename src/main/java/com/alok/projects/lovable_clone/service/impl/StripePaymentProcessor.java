package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.alok.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.alok.projects.lovable_clone.dto.subscription.PortalResponse;
import com.alok.projects.lovable_clone.entity.Plan;
import com.alok.projects.lovable_clone.entity.User;
import com.alok.projects.lovable_clone.error.ResourceNotFoundException;
import com.alok.projects.lovable_clone.repository.PlanRepository;
import com.alok.projects.lovable_clone.repository.UserRepository;
import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.PaymentProcessor;
import com.stripe.exception.StripeException;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StripePaymentProcessor implements PaymentProcessor {

    private final AuthUtil authUtil;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    @Value("${client.url}")
    private String frontendUrl;

    @Override
    public CheckoutResponse createCheckoutSessionUrl(CheckoutRequest request) {
        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Plan", request.planId().toString()));
        Long userId = authUtil.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User", userId.toString()));

        var params = SessionCreateParams.builder()
                .addLineItem(
                        SessionCreateParams.LineItem.builder().setPrice(plan.getStripePriceId()).setQuantity(1L).build()
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
         * so, when we set the striptCustomerId (if the user is already there in the subscription earlier) then it'll take that user's entry only and will not create another entry in the subscription for the user.
         * setting customerEmail will make sure that the user logged in in our system has same username as that inside Stripe.
         */
        try {
            String stripeCustomerId = user.getStripeCustomerId();
            if(stripeCustomerId == null || stripeCustomerId.isEmpty()) {
                params.setCustomerEmail(user.getUsername());
            } else {
                params.setCustomer(stripeCustomerId); // stripe customer id
            }

            Session session = Session.create(params.build()); /// making API call to the Stripe
            return new CheckoutResponse(session.getUrl());
        } catch (StripeException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public PortalResponse openCustomerPortal(CheckoutRequest request) {
        return null;
    }

    @Override
    public void handleWebhookEvent(String type, StripeObject stripeObject, Map<String, String> metadata) {
        System.out.println(metadata);
    }
}
