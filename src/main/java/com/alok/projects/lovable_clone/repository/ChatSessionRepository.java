package com.alok.projects.lovable_clone.repository;

import com.alok.projects.lovable_clone.entity.ChatSession;
import com.alok.projects.lovable_clone.entity.ids.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatSessionRepository extends JpaRepository<ChatSession, ChatSessionId> {
}
