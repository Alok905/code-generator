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

    /**
     * /// It'll not be needed because we'll store the content in ChatEvent (multiple types of chat messages type can be there)
     *
     * @Column(columnDefinition = "text", nullable = false)
     * String content;
     */


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    MessageRole role; // who sent the message (user/system) // for system, the different types are there in ChatEvents

    @OneToMany(mappedBy = "chatMessage", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("sequenceOrder ASC") /// if you want to order according to a particular field (here: sequenceOrder)
    List<ChatEvent> events;

    Integer tokensUsed = 0;

    @CreationTimestamp
    Instant createdAt;
}
