package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.dto.chat.ChatResponse;
import com.alok.projects.lovable_clone.entity.ChatMessage;
import com.alok.projects.lovable_clone.entity.ChatSession;
import com.alok.projects.lovable_clone.entity.ids.ChatSessionId;
import com.alok.projects.lovable_clone.mapper.ChatMapper;
import com.alok.projects.lovable_clone.repository.ChatMessageRepository;
import com.alok.projects.lovable_clone.repository.ChatSessionRepository;
import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final ChatMapper chatMapper;
    private final AuthUtil authUtil;

    @Override
    public List<ChatResponse> getProjectChatHistory(Long projectId) {

        Long userId = authUtil.getCurrentUserId();
        ChatSession chatSession = chatSessionRepository.getReferenceById(new ChatSessionId(userId, projectId));

        List<ChatMessage> chatMessageList = chatMessageRepository.findByChatSession(chatSession);

        return chatMapper.fromListOfChatMessages(chatMessageList);
    }
}
