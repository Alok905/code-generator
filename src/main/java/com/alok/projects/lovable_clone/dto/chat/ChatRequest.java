package com.alok.projects.lovable_clone.dto.chat;

public record ChatRequest(
        String message,
        Long projectId
) {
}
