package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.security.AuthUtil;
import com.alok.projects.lovable_clone.service.AiGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;

    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public Flux<String> streamResponse(String userMessage, Long projectId) {
        Long userId = authUtil.getCurrentUserId();

        createChatSessionIfNotExists(projectId, userId);

        Map<String, Object> advisorParams = Map.of(
                "userId", userId,
                "projectId", projectId
        );

        StringBuilder fullResponseBuffer = new StringBuilder();

        return chatClient.prompt()
                .system("SYSTEM_PROMPT_HERE")
                .user(userMessage)
                .advisors(
                        advisorSpec -> {
                            advisorSpec.params(advisorParams);
                        }
                )
                .stream()
                .chatResponse()
                /// if you call subscribe(onComplete, onNext, onError ..etc) method then it'll directly trigger the flux
                /// doOnNext, doOnComplete, doOnError will be executed when subscribe() is called. these will not trigger flux
                .doOnNext(response -> {
                    String content = response.getResult().getOutput().getText();
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(() -> {
                        parseAndSaveFiles(fullResponseBuffer.toString(), projectId);
                    });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: " + projectId, error))
                .map(response -> Objects.requireNonNull(response.getResult().getOutput().getText()));
    }

    private void parseAndSaveFiles(String fullResponse, Long projectId) {
    }

    private void createChatSessionIfNotExists(Long projectId, Long userId) {
    }
}
