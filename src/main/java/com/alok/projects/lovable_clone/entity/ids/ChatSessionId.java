package com.alok.projects.lovable_clone.entity.ids;

import jakarta.persistence.Embeddable;

import java.io.Serializable;

@Embeddable
public class ChatSessionId implements Serializable {
    private Long userId;
    private Long projectId;

//    public ChatSessionId(){}
//
//    public ChatSessionId(Long userId, Long projectId) {
//        this.userId = userId;
//        this.projectId = projectId;
//    }
}
