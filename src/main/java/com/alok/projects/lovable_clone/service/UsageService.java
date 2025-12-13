package com.alok.projects.lovable_clone.service;

import com.alok.projects.lovable_clone.dto.subscription.PlanLimitsResponse;
import com.alok.projects.lovable_clone.dto.subscription.UsageTodayResponse;
import org.jspecify.annotations.Nullable;

public interface UsageService {
    UsageTodayResponse getTodayUsageOfUser(Long userId);

    PlanLimitsResponse getCurrentSubscriptionLimits(Long userId);
}
