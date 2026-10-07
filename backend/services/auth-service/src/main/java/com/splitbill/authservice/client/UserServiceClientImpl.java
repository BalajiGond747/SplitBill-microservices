package com.splitbill.authservice.client;

import com.splitbill.authservice.dto.request.UpdateProfileRequest;
import com.splitbill.authservice.dto.response.ApiResponse;
import com.splitbill.authservice.dto.response.UserResponse;
import com.splitbill.authservice.service.UserServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements UserServiceClient {

    private static final ParameterizedTypeReference<ApiResponse<UserResponse>> USER_RESPONSE_TYPE = new ParameterizedTypeReference<>() {
    };

    private final RestClient userServiceRestClient;

    @Override
    public UserResponse createUser(String name, String email, String username) {

        ApiResponse<UserResponse> response = userServiceRestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UserCreatePayload(name, email, username))
                .retrieve()
                .body(USER_RESPONSE_TYPE);

        return response.getData();
    }

    @Override
    public UserResponse getUserByUsername(String username) {

        try {

            ApiResponse<UserResponse> response = userServiceRestClient.get()
                    .uri("/api/v1/users/username/{username}", username)
                    .retrieve()
                    .body(USER_RESPONSE_TYPE);

            return response.getData();

        } catch (RestClientResponseException exception) {

            if (exception.getStatusCode()
                    .value() == 404) {
                return null;
            }

            throw exception;
        }
    }

    @Override
    public UserResponse getUserByEmail(String email) {

        try {

            ApiResponse<UserResponse> response = userServiceRestClient.get()
                    .uri("/api/v1/users/email/{email}", email)
                    .retrieve()
                    .body(USER_RESPONSE_TYPE);

            return response.getData();

        } catch (RestClientResponseException exception) {

            if (exception.getStatusCode()
                    .value() == 404) {
                return null;
            }

            throw exception;
        }
    }

    @Override
    public UserResponse getUserById(Long userId) {

        ApiResponse<UserResponse> response = userServiceRestClient.get()
                .uri("/api/v1/users/{id}", userId)
                .retrieve()
                .body(USER_RESPONSE_TYPE);

        return response.getData();
    }

    @Override
    public UserResponse updateUser(Long userId, UpdateProfileRequest request) {

        ApiResponse<UserResponse> response = userServiceRestClient.put()
                .uri("/api/v1/users/{id}", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(USER_RESPONSE_TYPE);

        return response.getData();
    }

    @Override
    public void deactivateUser(Long userId) {

        userServiceRestClient.delete()
                .uri("/api/v1/users/{id}", userId)
                .retrieve()
                .toBodilessEntity();
    }

    private record UserCreatePayload(String name, String email, String username) {
    }
}