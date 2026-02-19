package com.alok.projects.lovable_clone.repository;

import com.alok.projects.lovable_clone.entity.ChatMessage;
import com.alok.projects.lovable_clone.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            SELECT DISTINCT cm FROM ChatMessage cm
            LEFT JOIN FETCH cm.events e
            WHERE cm.chatSession = :chatSession
            ORDER BY cm.createdAt ASC, e.sequenceOrder ASC
            """)
    List<ChatMessage> findByChatSession(ChatSession chatSession);
}
