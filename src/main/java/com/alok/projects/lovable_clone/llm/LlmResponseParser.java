package com.alok.projects.lovable_clone.llm;

import com.alok.projects.lovable_clone.entity.ChatEvent;
import com.alok.projects.lovable_clone.entity.ChatMessage;
import com.alok.projects.lovable_clone.enums.ChatEventType;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class LlmResponseParser {

    /**
     * Regex Breakdown:
     * Group 1: Opening Tag (<tag ...>)
     * Group 2: Tag Name (message|file|tool)
     * Group 3: Attributes part (e.g., ' path="foo"' or ' args="a,b"')
     * Group 4: Content (The stuff inside)
     * Group 5: Closing Tag (</tag>)
     */

    /**
     * the idea is to first get the content i.e. <tag att1=val1>...content...</tag>
     * after getting this, again find the attributes of the tags
     */
    private static final Pattern GENERIC_TAG_PATTERN = Pattern.compile(
            "(<(message|file|tool)([^>]*)>)([\\s\\S]*?)(</\\2>)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL
    );

    // Helper to extract specific attributes (path="..." or args="...") from Group 3
    private static final Pattern ATTRIBUTE_PATTERN = Pattern.compile(
            "(path|args)=\"([^\"]+)\""
    );

    public List<ChatEvent> parseChatEvents(String fullResponse, ChatMessage parentMessage) {

        List<ChatEvent> chatEvents = new ArrayList<>();
        int orderCounter = 1;

        Matcher matcher = GENERIC_TAG_PATTERN.matcher(fullResponse);

        /// first get each tag and content combined
        while (matcher.find()) {
            ///  because matcher.group(1) will give <tag_name args...>
            String tagName = matcher.group(2);
            String attributes = matcher.group(3);
            String content = matcher.group(4);

            /// Extract attributes map
            Map<String, String> attrMap = parseAttributes(attributes);

            ChatEvent.ChatEventBuilder builder = ChatEvent.builder()
                    .chatMessage(parentMessage)
                    .content(content)
                    .sequenceOrder(orderCounter++);

            switch (tagName) {
                // <message phase="start"> .... </message>
                // <message phase="completed"> .... </message>
                case "message" -> builder.type(ChatEventType.MESSAGE);  // in this syntax, you don't need to write break at each "case"
                // <file path="src/hooks/useTheme.ts"> ... </file>
                case "file" -> {
                    builder.type(ChatEventType.FILE_EDIT);
                    builder.filePath(attrMap.get("path"));
                }
                // <tool args="src/components/ThemeToggle.tsx,src/components/Navbar.tsx,src/App.tsx,src/index.css"> ... </tool>
                case "tool" -> {
                    builder.type(ChatEventType.TOOL_LOG);
                    builder.metadata(attrMap.get("args")); // store the raw file list in metadata
                }
                default -> { continue; }
            }
            chatEvents.add(builder.build());
        }
        return chatEvents;
    }

    Map<String, String> parseAttributes(String attributeString) {
        Map<String, String> attributes = new HashMap<>();
        if (attributeString == null) return attributes;

        Matcher matcher = ATTRIBUTE_PATTERN.matcher(attributeString);
        while (matcher.find()) {
            attributes.put(matcher.group(1), matcher.group(2));
        }
        return attributes;
    }

}
