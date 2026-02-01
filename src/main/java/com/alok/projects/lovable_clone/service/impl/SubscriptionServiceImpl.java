package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.alok.projects.lovable_clone.entity.Plan;
import com.alok.projects.lovable_clone.entity.Subscription;
import com.alok.projects.lovable_clone.entity.User;
import com.alok.projects.lovable_clone.enums.SubscriptionStatus;
import com.alok.projects.lovable_clone.error.ResourceNotFoundException;
import com.alok.projects.lovable_clone.mapper.SubscriptionMapper;
import com.alok.projects.lovable_clone.repository.PlanRepository;
import com.alok.projects.lovable_clone.repository.ProjectMemberRepository;
import com.alok.projects.lovable_clone.repository.SubscriptionRepository;
import com.alok.projects.lovable_clone.repository.UserRepository;
import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final AuthUtil authUtil;
    private final SubscriptionMapper subscriptionMapper;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final PlanRepository planRepository;
    private final ProjectMemberRepository projectMemberRepository;


    // constants
    private final Integer FREE_TIER_PROJECTS_ALLOWED = 1;


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
    @Transactional
    public void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus subscriptionStatus, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        boolean hasSubscriptionUpdated = false;
        List<String> updations = new ArrayList<>();

        if(subscriptionStatus != null && subscriptionStatus != subscription.getStatus()) {
            subscription.setStatus(subscriptionStatus);
            updations.add("gatewaySubscriptionId: " + subscriptionStatus);
            hasSubscriptionUpdated = true;
        }

        if(periodStart != null && !periodStart.equals(subscription.getCurrentPeriodStart())) {
            subscription.setCurrentPeriodStart(periodStart);
            updations.add("periodStart: " + periodStart);
            hasSubscriptionUpdated = true;
        }

        if(periodEnd != null && !periodEnd.equals(subscription.getCurrentPeriodEnd())) {
            subscription.setCurrentPeriodEnd(periodEnd);
            updations.add("periodEnd: " + periodEnd);
            hasSubscriptionUpdated = true;
        }

        if(cancelAtPeriodEnd != null && cancelAtPeriodEnd != subscription.getCancelAtPeriodEnd()) {
            subscription.setCancelAtPeriodEnd(cancelAtPeriodEnd);
            updations.add("cancelAtPeriodEnd: " + cancelAtPeriodEnd);
            hasSubscriptionUpdated = true;
        }

        if(planId != null && !planId.equals(subscription.getPlan().getId())) {
            Plan plan = getPlan(planId);
            subscription.setPlan(plan);
            updations.add("planId: " + planId);
            hasSubscriptionUpdated = true;
        }

        /// save() only changes the ENTITY STATE — it does NOT commit to DB. so, it works even without writing @Transactional
        if(hasSubscriptionUpdated) {
            log.debug("Subscription has been updated: {}", gatewaySubscriptionId);
            updations.forEach(updation -> {
                log.debug("Updation values: {}", updation);
            });
            subscriptionRepository.save(subscription);
        }
    }

    @Override
    public void cancelSubscription(String gatewaySubscriptionId) {
        Subscription subscription = getSubscription(gatewaySubscriptionId);
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscriptionRepository.save(subscription);
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
        Subscription subscription = getSubscription(gatewaySubscriptionId);

        if(subscription.getStatus() == SubscriptionStatus.PAST_DUE) {
            log.debug("Subscription is already PastDue, gatewaySubscriptionId: {}", gatewaySubscriptionId);
            return;
        }
        subscription.setStatus(SubscriptionStatus.PAST_DUE);
        subscriptionRepository.save(subscription);

        // Notify user with email or something else.
    }

    @Override
    public boolean canCreateNewProject() {
        Long userId = authUtil.getCurrentUserId();
        SubscriptionResponse currentSubscription = getCurrentSubscription();

        int countOfOwnedProjects = projectMemberRepository.countProjectOwnedByUser(userId);

        if(currentSubscription.plan() == null) {
            return countOfOwnedProjects < FREE_TIER_PROJECTS_ALLOWED;
        }

        return countOfOwnedProjects < currentSubscription.plan().maxProjects();
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
