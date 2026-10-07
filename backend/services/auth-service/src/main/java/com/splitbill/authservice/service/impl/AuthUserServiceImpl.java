package com.splitbill.authservice.service.impl;

import com.splitbill.authservice.dto.request.ChangePasswordRequest;
import com.splitbill.authservice.dto.request.LoginRequest;
import com.splitbill.authservice.dto.request.RegisterRequest;
import com.splitbill.authservice.dto.request.UpdateProfileRequest;
import com.splitbill.authservice.dto.response.AuthResponse;
import com.splitbill.authservice.dto.response.UserResponse;
import com.splitbill.authservice.entity.AuthUser;
import com.splitbill.authservice.exception.AuthenticationFailureException;
import com.splitbill.authservice.exception.DuplicateResourceException;
import com.splitbill.authservice.exception.ResourceNotFoundException;
import com.splitbill.authservice.mapper.AuthUserMapper;
import com.splitbill.authservice.repository.AuthUserRepository;
import com.splitbill.authservice.security.GoogleTokenVerifier;
import com.splitbill.authservice.security.JwtService;
import com.splitbill.authservice.service.AuthUserService;
import com.splitbill.authservice.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthUserServiceImpl implements AuthUserService {

    private final AuthUserRepository authUserRepository;
    private final AuthUserMapper authUserMapper;
    private final UserServiceClient userServiceClient;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleTokenVerifier googleTokenVerifier;

    @Override
    public AuthResponse register(RegisterRequest request) {

        UserResponse existingUser = userServiceClient.getUserByUsername(request.getUsername());

        if (existingUser != null) {
            throw new DuplicateResourceException("Username '" + request.getUsername() + "' already exists");
        }

        UserResponse user = userServiceClient.createUser(request.getName(), request.getEmail(), request.getUsername());

        try {

            if (authUserRepository.existsByUserId(user.getId())) {
                throw new DuplicateResourceException("Authentication record already exists for user");
            }

            String encodedPassword = passwordEncoder.encode(request.getPassword());

            AuthUser authUser = authUserMapper.toEntity(user.getId(), encodedPassword);

            authUserRepository.save(authUser);

            return createAuthResponse(authUser, user);

        } catch (Exception exception) {

            try {
                userServiceClient.deactivateUser(user.getId());
            } catch (Exception ignored) {
            }

            throw exception;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        String username = request.getUsername()
                .trim();

        UserResponse user = userServiceClient.getUserByUsername(username);


        if (user == null) {
            throw new AuthenticationFailureException("Invalid username or password");
        }

        if (!Boolean.TRUE.equals(user.getActive())) {
            throw new AuthenticationFailureException("Invalid username or password");
        }

        AuthUser authUser = authUserRepository.findByUserId(user.getId())
                .orElseThrow(() -> new AuthenticationFailureException("Invalid username or password"));

        if (!Boolean.TRUE.equals(authUser.getEnabled())) {
            throw new AuthenticationFailureException("Invalid username or password");
        }


        if (authUser.getProvider() != AuthUser.Provider.LOCAL) {

            throw new AuthenticationFailureException("This account uses Google Sign-In. Please continue with Google.");
        }

        if (!passwordEncoder.matches(request.getPassword(), authUser.getPassword())) {

            throw new AuthenticationFailureException("Invalid username or password");
        }

        return createAuthResponse(authUser, user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(Long userId) {

        return userServiceClient.getUserById(userId);
    }

    @Override
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {

        UserResponse currentUser = userServiceClient.getUserById(userId);

        if (currentUser == null) {
            throw new ResourceNotFoundException("User not found");
        }

        return userServiceClient.updateUser(userId, request);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        AuthUser authUser = authUserRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Authentication record not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), authUser.getPassword())) {

            throw new AuthenticationFailureException("Current password is incorrect");
        }

        authUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        authUserRepository.save(authUser);
    }

    @Override
    public AuthResponse loginWithGoogle(String credential) {

        GoogleTokenVerifier.GoogleUserInfo googleUser = googleTokenVerifier.verify(credential);


        AuthUser authUser = authUserRepository.findByProviderAndProviderSubject(AuthUser.Provider.GOOGLE, googleUser.subject())
                .orElse(null);

        UserResponse user;

        if (authUser != null) {

            if (!Boolean.TRUE.equals(authUser.getEnabled())) {
                throw new AuthenticationFailureException("Account is disabled");
            }

            user = userServiceClient.getUserById(authUser.getUserId());

            if (user == null || !Boolean.TRUE.equals(user.getActive())) {

                throw new AuthenticationFailureException("Account is inactive");
            }

        } else {


            user = userServiceClient.getUserByEmail(googleUser.email());

            if (user != null) {

                if (!Boolean.TRUE.equals(user.getActive())) {
                    throw new AuthenticationFailureException("Account is inactive");
                }


                authUser = authUserRepository.findByUserId(user.getId())
                        .orElse(null);

                if (authUser == null) {


                    authUser = createGoogleAuthUser(user.getId(), googleUser.subject());

                } else if (!Boolean.TRUE.equals(authUser.getEnabled())) {

                    throw new AuthenticationFailureException("Account is disabled");
                }

            } else {


                String username = buildGoogleUsername(googleUser.email(), googleUser.subject());

                String name = googleUser.name();

                if (name == null || name.isBlank()) {

                    name = googleUser.email()
                            .split("@")[0];
                }

                user = userServiceClient.createUser(name, googleUser.email(), username);

                authUser = createGoogleAuthUser(user.getId(), googleUser.subject());
            }
        }

        return createAuthResponse(authUser, user);
    }

    private AuthUser createGoogleAuthUser(Long userId, String googleSubject) {

        String randomPassword = passwordEncoder.encode(java.util.UUID.randomUUID()
                .toString());

        AuthUser authUser = AuthUser.builder()
                .userId(userId)
                .password(randomPassword)
                .role(AuthUser.Role.USER)
                .provider(AuthUser.Provider.GOOGLE)
                .providerSubject(googleSubject)
                .enabled(true)
                .build();

        return authUserRepository.save(authUser);
    }

    private AuthResponse createAuthResponse(AuthUser authUser, UserResponse user) {

        String accessToken = jwtService.generateAccessToken(authUser.getUserId(), user.getUsername(), authUser.getRole()
                .name());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirySeconds())
                .user(user)
                .build();
    }

    private String buildGoogleUsername(String email, String googleSubject) {

        String localPart = email.substring(0, email.indexOf('@'));

        String sanitized = localPart.toLowerCase()
                .replaceAll("[^a-z0-9_]", "_");

        if (sanitized.length() > 40) {
            sanitized = sanitized.substring(0, 40);
        }

        String username = "google_" + sanitized;

        UserResponse existing = userServiceClient.getUserByUsername(username);

        if (existing == null) {
            return username;
        }

        String suffix = googleSubject.substring(0, Math.min(8, googleSubject.length()));

        String candidate = "google_" + sanitized + "_" + suffix;

        if (candidate.length() > 50) {
            candidate = candidate.substring(0, 50);
        }

        return candidate;
    }
}