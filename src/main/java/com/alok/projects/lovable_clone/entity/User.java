package com.alok.projects.lovable_clone.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;


@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "users")
public class User implements UserDetails {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String username;
    String password;
    String name;

    @Column(unique = true)
    String stripeCustomerId;

    @CreationTimestamp
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt; // soft delete (database entry will not be deleted)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

//    @OneToMany(mappedBy = "user")
//    List<Subscription> subscriptions;
//
//    @OneToMany(mappedBy = "user")
//    List<UsageLog> usageLogs;

//    @OneToMany(
//            mappedBy = "user"
//    )
//    List<ProjectMember> projectMembers;
//
//    @OneToMany(mappedBy = "owner")
//    List<Project> ownedProjects;
//
//    @OneToMany(
//            mappedBy = "createdBy"
//    )
//    List<ProjectFile> createdFiles;
//
//    @OneToMany(
//            mappedBy = "updatedBy"
//    )
//    List<ProjectFile> updatedFiles;
}
