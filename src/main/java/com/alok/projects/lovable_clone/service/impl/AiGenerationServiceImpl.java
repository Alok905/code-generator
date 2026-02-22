package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.entity.*;
import com.alok.projects.lovable_clone.entity.ids.ChatSessionId;
import com.alok.projects.lovable_clone.enums.ChatEventType;
import com.alok.projects.lovable_clone.enums.MessageRole;
import com.alok.projects.lovable_clone.error.ResourceNotFoundException;
import com.alok.projects.lovable_clone.llm.LlmResponseParser;
import com.alok.projects.lovable_clone.llm.PromptUtils;
import com.alok.projects.lovable_clone.llm.advisors.FileTreeContextAdvisor;
import com.alok.projects.lovable_clone.llm.tools.CodeGenerationTools;
import com.alok.projects.lovable_clone.repository.*;
import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.AiGenerationService;
import com.alok.projects.lovable_clone.service.ProjectFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final ProjectFileService projectFileService;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatSessionRepository chatSessionRepository;
    private final LlmResponseParser llmResponseParser;
    private final ChatEventRepository chatEventRepository;

    private static final Pattern FILE_TAG_PATTERN = Pattern.compile(
            "<file path=\"([^\"]+)\">(.*?)</file>",
            Pattern.DOTALL
    );


    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> streamResponse(String userMessage, Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        ChatSession chatSession = createChatSessionIfNotExists(projectId, userId);

        AtomicReference<Long> startTime = new AtomicReference<>(System.currentTimeMillis());
        AtomicReference<Long> endTime = new AtomicReference<>(0L);


        /// it is being attached to the chat client because we'll need the project ID to get the file-tree of this project.
        /// which can be fed to AI to let it get know about the files we do have right now.
        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId
        );

        StringBuilder fullResponseBuffer = new StringBuilder();

        /// its just a tool to provide the content of the files that LLM request during tool calling
        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectFileService, projectId);

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
                .user(userMessage)
                .advisors(
                        advisorSpec -> {
                            advisorSpec.params(advisorParams);
                            advisorSpec.advisors(fileTreeContextAdvisor);
                        }
                )
                .tools(
                        codeGenerationTools
                )
                .stream()
                .chatResponse()
                /// if you call subscribe(onComplete, onNext, onError ..etc) method then it'll directly trigger the flux
                /// doOnNext, doOnComplete, doOnError will be executed when subscribe() is called. these will not trigger flux
                .doOnNext(response -> {
                    String content = response.getResult().getOutput().getText();

                    /// because when we'll get first response, it means LLM has completed its thinking process.
                    if (content != null && !content.isEmpty() && endTime.get() == 0) {
                        endTime.set(System.currentTimeMillis());
                    }

                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(() -> {
//                        parseAndSaveFiles(fullResponseBuffer.toString(), projectId);

                        long duration = (endTime.get() - startTime.get()) / 1000;
                        /// after the stream is completed, extract the different types of contents like userMessage and systemMessage
                        /// system message contains many types of contents (which are called "events" in this project) like tool call, file read, file generation etc.
                        finalizeChats(userMessage, chatSession, fullResponseBuffer.toString(), duration);
                    });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: {}", projectId))
                /// map runs after every onNext is called (doOnNext); it transforms and send it as a response to the client
                .map(response -> {
                    String content = response.getResult().getOutput().getText();
                    if (content != null) return content;
                    return "";
                });
        /**
         * instead of creating a custom advisor for adding file-tree, we could have just written
         * PromptUtils.CODE_GENERATION_SYSTEM_PROMPT + projectFileRepository.getFileTree().toString() , but its better to use the things properly.
         *  because any ways advisors do nothing but modifying the prompt.
         * and system prompts should be there in the beginning and user prompts should be at the end.
         */
    }

    /// the messages can be easily fetched sequentially according to creation time, so no need to keep a "sequenceOrder" field in ChatMessage entity unline ChatEvent entity
    private void finalizeChats(String userMessage, ChatSession chatSession, String fullText, Long duration) {
        Long projectId = chatSession.getProject().getId();
        /// first store the user's message
        chatMessageRepository.save(
                ChatMessage.builder()
                        .chatSession(chatSession)
                        .role(MessageRole.USER)
                        .content(userMessage)
                        .build()
        );

        /// create assistant chat message and create chat events associated with it
        ChatMessage assistantMessage = ChatMessage.builder()
                .role(MessageRole.ASSISTANT)
                .chatSession(chatSession)
                .content("assistant content")
                .build();

        assistantMessage = chatMessageRepository.save(assistantMessage);

        List<ChatEvent> chatEventList = llmResponseParser.parseChatEvents(fullText, assistantMessage);
        chatEventList.addFirst(
                ChatEvent.builder()
                        .type(ChatEventType.THOUGHT)
                        .chatMessage(assistantMessage)
                        .content(String.format("Thought for %ds", duration))
                        .sequenceOrder(0)
//                        .content("Thought for " + duration + "s")
                        .build()
        );

        chatEventList.stream()
                .filter(e -> e.getType() == ChatEventType.FILE_EDIT)
                .forEach(e -> projectFileService.saveFile(projectId, e.getFilePath(), e.getContent()));

        chatEventRepository.saveAll(chatEventList);
    }


    private void parseAndSaveFiles(String fullResponse, Long projectId) {
        String dummy = """
                <message>something something</message>
                <file path="src/App.jsx">
                    import App from './App.jsx';
                    ....
                    ....
                </file>
                <message>something something</message>
                <file path="src/App.jsx">
                    import App from './App.jsx';
                    ....
                    ....
                </file>
                """;
        Matcher matcher = FILE_TAG_PATTERN.matcher(fullResponse);
        while (matcher.find()) {
            String filePath = matcher.group(1);
            String fileContent = matcher.group(2);

            projectFileService.saveFile(projectId, filePath, fileContent);
        }
    }

    private ChatSession createChatSessionIfNotExists(Long projectId, Long userId) {
        ChatSessionId chatSessionId = new ChatSessionId(userId, projectId);
        ChatSession chatSession = chatSessionRepository.findById(chatSessionId)
                .orElse(null);

        if (chatSession == null) {
            Project project = projectRepository.findById(projectId)
                    .orElseThrow(() -> new ResourceNotFoundException("Project", projectId.toString()));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

            chatSession = ChatSession.builder()
                    .id(chatSessionId)
                    .project(project)
                    .user(user)
                    .build();

            chatSession = chatSessionRepository.save(chatSession);
        }
        return chatSession;
    }
}
