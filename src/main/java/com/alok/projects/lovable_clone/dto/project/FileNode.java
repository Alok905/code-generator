package com.alok.projects.lovable_clone.dto.project;

import org.jspecify.annotations.NonNull;

public record FileNode(
        String path
) {
    @Override
    public @NonNull String toString() {
        return path;
    }
}
