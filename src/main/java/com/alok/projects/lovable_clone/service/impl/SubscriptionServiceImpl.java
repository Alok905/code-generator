package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.subscription.CheckoutRequest;
import com.alok.projects.lovable_clone.dto.subscription.CheckoutResponse;
import com.alok.projects.lovable_clone.dto.subscription.PortalResponse;
import com.alok.projects.lovable_clone.dto.subscription.SubscriptionResponse;
import com.alok.projects.lovable_clone.service.SubscriptionService;
import org.springframework.stereotype.Service;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {
    @Override
    public SubscriptionResponse getCurrentSubscription(Long userId) {
        return null;
    }
}
