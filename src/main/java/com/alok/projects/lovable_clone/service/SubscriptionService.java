package com.alok.projects.lovable_clone.service;

import com.alok.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.alok.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.alok.projects.lovable_clone.dto.subscription.PortalResponse;
import com.alok.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.alok.projects.lovable_clone.entity.Subscription;
import com.alok.projects.lovable_clone.enums.SubscriptionStatus;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.Optional;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();

    void activateSubscription(Long userId, Long planId, String gatewaySubscriptionId, String customerId);

    void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus subscriptionStatus, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String gatewaySubscriptionId);

    void renewSubscriptionPeriod(String gatewaySubscriptionId, Instant periodStart, Instant periodEnd);

    void markSubscriptionPastDue(String gatewaySubscriptionId);
}
