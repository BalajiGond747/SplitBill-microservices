package com.splitbill.authservice.service;

import com.splitbill.authservice.dto.request.ChangePasswordRequest;
import com.splitbill.authservice.dto.request.LoginRequest;
import com.splitbill.authservice.dto.request.RegisterRequest;
import com.splitbill.authservice.dto.request.UpdateProfileRequest;
import com.splitbill.authservice.dto.response.AuthResponse;
import com.splitbill.authservice.dto.response.UserResponse;

public interface AuthUserService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    UserResponse getCurrentUser(Long userId);

    UserResponse updateProfile(Long userId, UpdateProfileRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    AuthResponse loginWithGoogle(String credential);
}