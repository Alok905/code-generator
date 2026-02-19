package com.alok.projects.lovable_clone.mapper;

import com.alok.projects.lovable_clone.dto.chat.ChatResponse;
import com.alok.projects.lovable_clone.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {

    List<ChatResponse> fromListOfChatMessages(List<ChatMessage> chatMessageList);
}
