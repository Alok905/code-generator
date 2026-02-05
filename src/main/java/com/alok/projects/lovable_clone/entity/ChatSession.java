package com.alok.projects.lovable_clone.entity;

import com.alok.projects.lovable_clone.entity.ids.ChatSessionId;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;

// one user will be having only one chat session per project
// as the project is collaborative, all the members will be having one chat session each for a single project.

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chat_sessions")
public class ChatSession {

    @EmbeddedId
    private ChatSessionId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("projectId")
    @JoinColumn(
            name = "project_id", nullable = false, updatable = false
    )
    Project project;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(
            name = "user_id", nullable = false, updatable = false
    )
    User user;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    Instant createdAt;

    @UpdateTimestamp
    Instant updatedAt;

    Instant deletedAt;  // soft delete

//    @OneToMany(mappedBy = "chatSession")
//    List<ChatMessage> chatMessages;
}
