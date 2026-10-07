package com.splitbill.authservice.controller;

import com.splitbill.authservice.dto.request.*;
import com.splitbill.authservice.dto.response.ApiResponse;
import com.splitbill.authservice.dto.response.AuthResponse;
import com.splitbill.authservice.dto.response.MessageResponse;
import com.splitbill.authservice.dto.response.UserResponse;
import com.splitbill.authservice.service.AuthUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthUserService authUserService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authUserService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {

        AuthResponse response = authUserService.login(request);

        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(Authentication authentication) {

        Long userId = Long.valueOf(authentication.getName());

        UserResponse response = authUserService.getCurrentUser(userId);

        return ResponseEntity.ok(ApiResponse.success("Current user fetched successfully", response));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest request) {

        Long userId = Long.valueOf(authentication.getName());

        UserResponse response = authUserService.updateProfile(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<MessageResponse>> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest request) {

        Long userId = Long.valueOf(authentication.getName());

        authUserService.changePassword(userId, request);

        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", new MessageResponse("Password changed successfully")));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<MessageResponse>> logout() {

        return ResponseEntity.ok(ApiResponse.success("Logout successful", new MessageResponse("Logout successful")));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {

        AuthResponse response = authUserService.loginWithGoogle(request.getCredential());

        return ResponseEntity.ok(ApiResponse.success("Google login successful", response));
    }
}