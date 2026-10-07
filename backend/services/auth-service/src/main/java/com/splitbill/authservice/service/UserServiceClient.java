package com.splitbill.authservice.service;

import com.splitbill.authservice.dto.request.UpdateProfileRequest;
import com.splitbill.authservice.dto.response.UserResponse;

public interface UserServiceClient {

    UserResponse createUser(String name, String email, String username);

    UserResponse getUserByUsername(String username);

    UserResponse getUserByEmail(String email);

    UserResponse getUserById(Long userId);

    UserResponse updateUser(Long userId, UpdateProfileRequest request);

    void deactivateUser(Long userId);
}