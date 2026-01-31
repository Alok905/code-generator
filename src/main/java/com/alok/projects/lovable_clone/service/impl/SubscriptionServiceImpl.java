package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.alok.projects.lovable_clone.entity.Plan;
import com.alok.projects.lovable_clone.entity.Subscription;
import com.alok.projects.lovable_clone.entity.User;
import com.alok.projects.lovable_clone.enums.SubscriptionStatus;
import com.alok.projects.lovable_clone.error.ResourceNotFoundException;
import com.alok.projects.lovable_clone.mapper.SubscriptionMapper;
import com.alok.projects.lovable_clone.repository.PlanRepository;
import com.alok.projects.lovable_clone.repository.SubscriptionRepository;
import com.alok.projects.lovable_clone.repository.UserRepository;
import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final AuthUtil authUtil;
    private final SubscriptionMapper subscriptionMapper;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;


    @Override
    public SubscriptionResponse getCurrentSubscription() {

        Long userId = authUtil.getCurrentUserId();

        var currentSubscription = subscriptionRepository.findByUserIdAndStatusIn(
                        userId,
                        Set.of(
                                SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE,
                                SubscriptionStatus.TRIALING
                        )
                )
                .orElse(new Subscription());

        return subscriptionMapper.toSubscriptionResponse(currentSubscription);

    }

    @Override
//    @Transactional
    public void activateSubscription(Long userId, Long planId, String gatewaySubscriptionId, String customerId) {

        boolean exists = subscriptionRepository.existsByStripeSubscriptionId(gatewaySubscriptionId);
        if (exists) return;

        User user = getUser(userId);
        Plan plan = getPlan(planId);

        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(plan)
                .stripeSubscriptionId(gatewaySubscriptionId)
                .status(SubscriptionStatus.INCOMPLETE) /// the status will be updated in "renewSubscriptionPeriod" method; "invoice.paid" event
                .build();

        subscriptionRepository.save(subscription);
    }

    /// "gatewaySubscriptionId" because it is not the "subscriptionId" of our database; it is the id of payment gateway (stripe)
    @Override
    public void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus subscriptionStatus, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {

    }

    @Override
    public void cancelSubscription(String gatewaySubscriptionId) {

    }

    @Override
    public void renewSubscriptionPeriod(String gatewaySubscriptionId, Instant periodStart, Instant periodEnd) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        /// just in case an invoice.paid (stripe) event is triggered but it didn't get periodStart, then it'll set it as the current subscription's periodEnd maintaining a proper chain.
        Instant newStart = periodStart != null ? periodStart : subscription.getCurrentPeriodEnd();
        subscription.setCurrentPeriodStart(newStart);
        subscription.setCurrentPeriodEnd(periodEnd);

        if(subscription.getStatus() == SubscriptionStatus.PAST_DUE || subscription.getStatus() == SubscriptionStatus.INCOMPLETE) {
            subscription.setStatus(SubscriptionStatus.ACTIVE);
        }

        subscriptionRepository.save(subscription);
    }

    @Override
    public void markSubscriptionPastDue(String gatewaySubscriptionId) {

    }


    ///  utility methods ------------------------------------------------------
    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));
    }

    private Plan getPlan(Long planId) {
        return planRepository.findById(planId)
                .orElseThrow(() -> new ResourceNotFoundException("Plan", planId.toString()));
    }

    private Subscription getSubscription(String gatewaySubscriptionId) {
        return subscriptionRepository
                .findByStripeSubscriptionId(gatewaySubscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", gatewaySubscriptionId));
    }

}
