package com.alok.projects.lovable_clone.dto.chat;

import com.alok.projects.lovable_clone.enums.ChatEventType;
import jakarta.persistence.*;

public record ChatEventResponse(
        Long id,
        ChatEventType type,
        Integer sequenceOrder,
        String content,
        String filePath,
        String metadata
) {
}