package com.alok.projects.lovable_clone.entity;

import com.alok.projects.lovable_clone.enums.ChatEventType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

/// it is for the types of system message, in lovable you can see it'll display system's response, which files were changed, response time etc etc
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
@Table(name = "chat_events")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    ChatMessage chatMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ChatEventType type;

    /// the order of the events which will be displayed (thought will be first, system's message will be 2nd.. etc etc)
    @Column(nullable = false)
    Integer sequenceOrder;

    @Column(columnDefinition = "text") /// columnDefinition is used to specify the type of column in db explicitly. by default it is VARCHAR(255) but here we need to store huge data
    String content;

    /// for <file></file>
    /// <file path="src/hooks/useTheme.ts"> ... </file>
    String filePath; /// NULL unless file edit

    /// for <tool></tool>
    /// <tool args="src/components/ThemeToggle.tsx,src/components/Navbar.tsx,src/App.tsx,src/index.css"> ... </tool>
    @Column(columnDefinition = "text")
    String metadata;
}
