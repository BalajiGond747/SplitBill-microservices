package com.splitbill.userservice.service;

import com.splitbill.userservice.dto.request.UserCreateRequest;
import com.splitbill.userservice.dto.request.UserUpdateRequest;
import com.splitbill.userservice.dto.response.PageResponse;
import com.splitbill.userservice.dto.response.UserResponse;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserResponse createUser(UserCreateRequest request);

    UserResponse getUserById(Long id);

    PageResponse<UserResponse> getUsers(
            String name,
            Boolean active,
            Pageable pageable
    );

    UserResponse updateUser(Long id, UserUpdateRequest request);

    void deactivateUser(Long id);
}