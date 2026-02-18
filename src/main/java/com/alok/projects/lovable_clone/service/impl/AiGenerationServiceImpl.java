package com.alok.projects.lovable_clone.service.impl;

import com.alok.projects.lovable_clone.llm.PromptUtils;
import com.alok.projects.lovable_clone.llm.advisors.FileTreeContextAdvisor;
import com.alok.projects.lovable_clone.llm.tools.CodeGenerationTools;
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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiGenerationServiceImpl implements AiGenerationService {

    private final ChatClient chatClient;
    private final AuthUtil authUtil;
    private final ProjectFileService projectFileService;
    private final FileTreeContextAdvisor fileTreeContextAdvisor;

    private static final Pattern FILE_TAG_PATTERN = Pattern.compile(
            "<file path=\"([^\"]+)\">(.*?)</file>",
            Pattern.DOTALL
    );



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

        CodeGenerationTools codeGenerationTools = new CodeGenerationTools(projectFileService, projectId);

        return chatClient.prompt()
                .system(PromptUtils.CODE_GENERATION_SYSTEM_PROMPT)
//                .system("SYSTEM PROMPT")
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
                    fullResponseBuffer.append(content);
                })
                .doOnComplete(() -> {
                    Schedulers.boundedElastic().schedule(() -> {
                        parseAndSaveFiles(fullResponseBuffer.toString(), projectId);
                    });
                })
                .doOnError(error -> log.error("Error during streaming for projectId: {}", projectId))
                .map(response -> {
                    String content = response.getResult().getOutput().getText();
                    if(content != null) return content;
                    return "";
                });
        /**
         * instead of creating a custom advisor for adding filetree, we could have just written
         * PromptUtils.CODE_GENERATION_SYSTEM_PROMPT + projectFileRepository.getFileTree().toString() , but its better to use the things properly.
         *  because any ways advisors do nothing but modifying the prompt.
         * and system prompts should be there in the beginning and user prompts should be at the end.
         */
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

    private void createChatSessionIfNotExists(Long projectId, Long userId) {

    }
}
