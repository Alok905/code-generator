package com.alok.projects.lovable_clone.dto.subscription;

import com.alok.projects.lovable_clone.enums.SubscriptionStatus;

public record PlanResponse(
        Long id,
        String name,
        Integer maxProjects,
        Integer maxTokensPerDay,
        Boolean unlimitedAi,
        String price
) {
}
