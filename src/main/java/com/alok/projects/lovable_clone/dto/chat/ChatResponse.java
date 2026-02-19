package com.alok.projects.lovable_clone.dto.chat;

import com.alok.projects.lovable_clone.entity.ChatSession;
import com.alok.projects.lovable_clone.enums.MessageRole;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

public record ChatResponse(
        Long id,
        ChatSession chatSession,
        MessageRole role,
        List<ChatEventResponse> events,
        Integer tokensUsed,
        Instant createdAt
        ) {
}
