package com.alok.projects.lovable_clone.entity;

import com.alok.projects.lovable_clone.enums.SubscriptionStatus;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

// multiple types of plans can be there; for example, in hotstar 3 or 4 types of plans are there

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class Plan {
    Long id;

    String name;

    SubscriptionStatus status;

    String stripePriceId;
    Integer maxProjects;
    Integer maxTokensPerDay;
    Integer maxPreviews; // max number of previews allowed per plan
    Boolean unlimitedAi; // unlimited access to LLM, ignore maxTokensPerDay if true

    Boolean active;
}
