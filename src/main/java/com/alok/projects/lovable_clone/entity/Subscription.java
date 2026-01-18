package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

// Subscription will be depending upon users; "which user" took subscription of "which plan"

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class Subscription {
    @Id
    Long id;

    // many to one (owning)
    @ManyToOne
    @JoinColumn(
            name = "user_id"
    )
    User user;

    // one to one (inverse)
    @OneToOne
    @JoinColumn(
            name = "plan_id"
    )
    Plan plan;

//    String stripeCustomerId;
    String stripeSubscriptionId;

    Instant currentPeriodStart;
    Instant currentPeriodEnd;
    Boolean cancelAtPeriodEnd = false;

    Instant createdAt;
    Instant updatedAt;
}
