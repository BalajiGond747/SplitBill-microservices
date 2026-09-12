package com.splitbill.userservice.service.impl;

import com.splitbill.userservice.dto.request.UserCreateRequest;
import com.splitbill.userservice.dto.request.UserUpdateRequest;
import com.splitbill.userservice.dto.response.PageResponse;
import com.splitbill.userservice.dto.response.UserResponse;
import com.splitbill.userservice.entity.User;
import com.splitbill.userservice.exception.DuplicateResourceException;
import com.splitbill.userservice.exception.ResourceNotFoundException;
import com.splitbill.userservice.mapper.UserMapper;
import com.splitbill.userservice.repository.UserRepository;
import com.splitbill.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse createUser(UserCreateRequest request) {

        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists");
        }

        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new DuplicateResourceException("User with username '" + request.getUsername() + "' already exists");
        }

        User user = userMapper.toEntity(request);

        User savedUser = userRepository.save(user);

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id '" + id + "' not found"));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(String name, Boolean active, Pageable pageable) {

        Page<User> users;

        if (name != null && !name.isBlank() && active != null) {

            users = userRepository.findByActiveAndNameContainingIgnoreCase(active, name.trim(), pageable);

        } else if (name != null && !name.isBlank()) {

            users = userRepository.findByNameContainingIgnoreCase(name.trim(), pageable);

        } else if (active != null) {

            users = userRepository.findByActive(active, pageable);

        } else {

            users = userRepository.findAll(pageable);
        }

        List<UserResponse> content = users.getContent()
                .stream()
                .map(userMapper::toResponse)
                .toList();

        return new PageResponse<>(content, users.getNumber(), users.getSize(), users.getTotalElements(), users.getTotalPages(), users.isFirst(), users.isLast());
    }

    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id '" + id + "' not found"));

        userRepository.findByEmailIgnoreCase(request.getEmail())
                .ifPresent(existingUser -> {

                    if (!existingUser.getId()
                            .equals(id)) {
                        throw new DuplicateResourceException("User with email '" + request.getEmail() + "' already exists");
                    }
                });

        userRepository.findByUsernameIgnoreCase(request.getUsername())
                .ifPresent(existingUser -> {

                    if (!existingUser.getId()
                            .equals(id)) {
                        throw new DuplicateResourceException("User with username '" + request.getUsername() + "' already exists");
                    }
                });

        userMapper.updateEntity(user, request);

        User updatedUser = userRepository.save(user);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deactivateUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id '" + id + "' not found"));

        user.setActive(false);

        userRepository.save(user);
    }
}