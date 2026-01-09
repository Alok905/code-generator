package com.alok.projects.lovable_clone.dto.auth;

// it is used to get the profile details like getProfile controller method
public record UserProfileResponse(
        Long id,
        String username,
        String name
) {
}
