package com.alok.projects.lovable_clone.service;

import com.alok.projects.lovable_clone.dto.auth.AuthResponse;
import com.alok.projects.lovable_clone.dto.auth.LoginRequest;
import com.alok.projects.lovable_clone.dto.auth.SignupRequest;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
