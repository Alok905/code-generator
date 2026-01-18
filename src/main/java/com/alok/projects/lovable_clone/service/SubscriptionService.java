package com.alok.projects.lovable_clone.service;

import com.alok.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.alok.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.alok.projects.lovable_clone.dto.subscription.PortalResponse;
import com.alok.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription(Long userId);
}
