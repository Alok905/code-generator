package com.alok.projects.lovable_clone.entity;

import com.alok.projects.lovable_clone.entity.ids.ChatSessionId;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

// one user will be having only one chat session per project
// as the project is collaborative, all the members will be having one chat session each for a single project.

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
@Table(
        uniqueConstraints = {@UniqueConstraint(name = "user_project", columnNames = {"user_id", "project_id"})}
)
public class ChatSession {

//    @EmbeddedId
//    ChatSessionId id;
    @Id
    Long id;

    @ManyToOne
    @JoinColumn(
            name = "project_id"
    )
    Project project;

    @ManyToOne
    @JoinColumn(
            name = "user_id"
    )
    User user;

    String title;


    Instant createdAt;
    Instant updatedAt;
    Instant deletedAt;

    @OneToMany(mappedBy = "chatSession")
    List<ChatMessage> chatMessages;
}
