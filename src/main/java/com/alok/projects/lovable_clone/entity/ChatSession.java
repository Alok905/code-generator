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
public class ChatSession {

    @EmbeddedId
    ChatSessionId id;

    @ManyToOne
    @JoinColumn(
            name = "project_id"
    )
    @MapsId("projectId")
    Project project;

    @ManyToOne
    @JoinColumn(
            name = "user_id"
    )
    @MapsId("userId")
    User user;

    String title;


    Instant createdAt;
    Instant updatedAt;
    Instant deletedAt;

    @OneToMany(mappedBy = "chatSession")
    List<ChatMessage> chatMessages;
}
