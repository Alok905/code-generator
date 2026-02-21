package com.alok.projects.lovable_clone.llm.advisors;

import com.alok.projects.lovable_clone.dto.project.FileNode;
import com.alok.projects.lovable_clone.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileTreeContextAdvisor implements StreamAdvisor {

    private final ProjectFileService projectFileService;


    @Override
    public @NonNull Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        Map<String, Object> context = chatClientRequest.context();
        Long projectId = Long.parseLong(context.getOrDefault("projectId", 0).toString());
        ChatClientRequest augmentedChatClientRequest = augmentRequestWithFileTree(chatClientRequest, projectId);
        return streamAdvisorChain.nextStream(augmentedChatClientRequest);
    }

    /// this method is this big just to make sure the system prompt and file tree and user prompt will be in order to make use of LLM cache for cost optimization by reduction in usage of tokens.
    private ChatClientRequest augmentRequestWithFileTree(ChatClientRequest request, Long projectId) {

        List<Message> incomingMessages = request.prompt().getInstructions();

        Message systemMessage = incomingMessages.stream()
                .filter(m -> m.getMessageType() == MessageType.SYSTEM)
                .findFirst()
                .orElse(null);

        List<Message> userMessages = incomingMessages.stream()
                .filter(m -> m.getMessageType() != MessageType.SYSTEM)
                .toList(); /// it is not mutable

        /**
         * its to make sure that system message (which is constant for all prompts) will be in the beginning
         * & user message will be at the end. it'll ensure the proper cache (in llm) to reduce the token utilization.
         */
        List<Message> allMessages = new ArrayList<>();

        /// 1st: system prompt
        if(systemMessage != null) {
            allMessages.add(systemMessage);
        }

        List<FileNode> fileTree = projectFileService.getFileTree(projectId);
        String fileTreeContext = "\n\n ----- FILE_TREE -----\n" + fileTree.toString();

        /// 2nd: file tree
        allMessages.add(new SystemMessage(fileTreeContext));
        /// 3rd: user prompt
        allMessages.addAll(userMessages);

        /// it is just to mutate the ChatClientRequest
        return request.mutate()
                /// request.prompt().getOptions() will give a ChatOption object which contains the headers like temperature, topP, topK etc etc.
                .prompt(new Prompt(allMessages, request.prompt().getOptions()))
                .build();
    }

    @Override
    public @NonNull String getName() {
        return "FileTreeContextAdvisor";
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
