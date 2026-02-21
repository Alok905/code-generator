package com.alok.projects.lovable_clone.entity;

import com.alok.projects.lovable_clone.enums.MessageRole;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.List;

// Chat message is there inside the Chat session

@Entity
@FieldDefaults(level = AccessLevel.PRIVATE) // it'll make the fields private by default
@Getter
@Setter
@Table(name = "chat_messages")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "project_id", referencedColumnName = "project_id", nullable = false),
            @JoinColumn(name = "user_id", referencedColumnName = "user_id", nullable = false),
    })
    ChatSession chatSession;


    /// NULL for system message; in case of system message ChatEvent will keep the content.
    /// in case of User message, this content will be there.
    @Column(columnDefinition = "text")
    String content;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MessageRole role; // who sent the message (user/system) // for system, the different types are there in ChatEvents

    /// if you want to order according to a particular field (here: sequenceOrder)
    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC")
    List<ChatEvent> events;

    Integer tokensUsed = 0;

    @CreationTimestamp
    Instant createdAt;
}
