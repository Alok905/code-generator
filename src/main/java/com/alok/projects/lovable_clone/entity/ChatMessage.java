package com.alok.projects.lovable_clone.entity;

import com.alok.projects.lovable_clone.enums.MessageRole;
import jakarta.persistence.Entity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

// Chat message is there inside the Chat session

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
public class ChatMessage {
    Long id;

    ChatSession chatSession;

    String content;

    MessageRole role; // who sent the message

    String toolCalls; // JSON array of tools called

    Integer tokensUsed;

    Instant createdAt;
}
