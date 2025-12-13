package com.alok.projects.lovable_clone.service;

import com.alok.projects.lovable_clone.dto.auth.UserProfileResponse;

public interface UserService {
    UserProfileResponse getProfile(Long userId);
}
