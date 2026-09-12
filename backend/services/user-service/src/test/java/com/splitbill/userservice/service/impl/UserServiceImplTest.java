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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    private UserResponse userResponse;

    @BeforeEach
    void setUp() {

        user = User.builder()
                .name("Balaji Gond")
                .email("balaji.gond@example.com")
                .username("balaji")
                .active(true)
                .build();

        user.setId(1L);

        userResponse = new UserResponse(1L, "Balaji Gond", "balaji.gond@example.com", "balaji", true, null, null);
    }

    @Test
    void createUser_shouldCreateUserSuccessfully() {

        UserCreateRequest request = new UserCreateRequest();

        request.setName("Balaji Gond");
        request.setEmail("balaji.gond@example.com");
        request.setUsername("balaji");

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);

        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(false);

        when(userMapper.toEntity(request)).thenReturn(user);

        when(userRepository.save(user)).thenReturn(user);

        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.createUser(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("balaji.gond@example.com");

        verify(userRepository).existsByEmailIgnoreCase(request.getEmail());

        verify(userRepository).existsByUsernameIgnoreCase(request.getUsername());

        verify(userRepository).save(user);

        verify(userMapper).toResponse(user);
    }

    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {

        UserCreateRequest request = new UserCreateRequest();

        request.setName("Balaji Gond");
        request.setEmail("balaji.gond@example.com");
        request.setUsername("balaji");

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository).existsByEmailIgnoreCase(request.getEmail());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_shouldThrowException_whenUsernameAlreadyExists() {

        UserCreateRequest request = new UserCreateRequest();

        request.setName("Balaji Gond");
        request.setEmail("new.email@example.com");
        request.setUsername("balaji");

        when(userRepository.existsByEmailIgnoreCase(request.getEmail())).thenReturn(false);

        when(userRepository.existsByUsernameIgnoreCase(request.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("username");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_shouldReturnUser_whenUserExists() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.getUserById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(userRepository).findById(1L);

        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserById_shouldThrowException_whenUserDoesNotExist() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(999L)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");

        verify(userRepository).findById(999L);

        verifyNoInteractions(userMapper);
    }

    @Test
    void getUsers_shouldReturnPaginatedUsers() {

        Pageable pageable = PageRequest.of(0, 10, Sort.by("name")
                .ascending());

        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        when(userMapper.toResponse(user)).thenReturn(userResponse);

        PageResponse<UserResponse> result = userService.getUsers(null, null, pageable);

        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.isFirst()).isTrue();
        assertThat(result.isLast()).isTrue();

        assertThat(result.getContent()
                .get(0)
                .getId()).isEqualTo(1L);

        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUsers_shouldSearchByName() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findByNameContainingIgnoreCase("Balaji", pageable)).thenReturn(userPage);

        when(userMapper.toResponse(user)).thenReturn(userResponse);

        PageResponse<UserResponse> result = userService.getUsers("Balaji", null, pageable);

        assertThat(result.getContent()).hasSize(1);

        assertThat(result.getContent()
                .get(0)
                .getName()).isEqualTo("Balaji Gond");

        verify(userRepository).findByNameContainingIgnoreCase("Balaji", pageable);
    }

    @Test
    void getUsers_shouldFilterByActiveStatus() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findByActive(true, pageable)).thenReturn(userPage);

        when(userMapper.toResponse(user)).thenReturn(userResponse);

        PageResponse<UserResponse> result = userService.getUsers(null, true, pageable);

        assertThat(result.getContent()).hasSize(1);

        assertThat(result.getContent()
                .get(0)
                .getActive()).isTrue();

        verify(userRepository).findByActive(true, pageable);
    }

    @Test
    void getUsers_shouldSearchAndFilter() {

        Pageable pageable = PageRequest.of(0, 10);

        Page<User> userPage = new PageImpl<>(List.of(user));

        when(userRepository.findByActiveAndNameContainingIgnoreCase(true, "Balaji", pageable)).thenReturn(userPage);

        when(userMapper.toResponse(user)).thenReturn(userResponse);

        PageResponse<UserResponse> result = userService.getUsers("Balaji", true, pageable);

        assertThat(result.getContent()).hasSize(1);

        verify(userRepository).findByActiveAndNameContainingIgnoreCase(true, "Balaji", pageable);
    }

    @Test
    void updateUser_shouldUpdateSuccessfully() {

        UserUpdateRequest request = new UserUpdateRequest();

        request.setName("Balaji Updated");
        request.setEmail("balaji.updated@example.com");
        request.setUsername("balaji_updated");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.empty());

        when(userRepository.findByUsernameIgnoreCase(request.getUsername())).thenReturn(Optional.empty());

        when(userRepository.save(user)).thenReturn(user);

        when(userMapper.toResponse(user)).thenReturn(userResponse);

        UserResponse result = userService.updateUser(1L, request);

        assertThat(result).isNotNull();

        verify(userMapper).updateEntity(user, request);

        verify(userRepository).save(user);
    }

    @Test
    void updateUser_shouldThrowException_whenUserDoesNotExist() {

        UserUpdateRequest request = new UserUpdateRequest();

        request.setName("Test");
        request.setEmail("test@example.com");
        request.setUsername("testuser");

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(999L, request)).isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_shouldThrowException_whenEmailBelongsToAnotherUser() {

        UserUpdateRequest request = new UserUpdateRequest();

        request.setName("Updated");
        request.setEmail("existing@example.com");
        request.setUsername("updated");

        User anotherUser = User.builder()
                .name("Another User")
                .email("existing@example.com")
                .username("another")
                .active(true)
                .build();

        anotherUser.setId(2L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(userRepository.findByEmailIgnoreCase(request.getEmail())).thenReturn(Optional.of(anotherUser));

        assertThatThrownBy(() -> userService.updateUser(1L, request)).isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("email");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deactivateUser_shouldDeactivateSuccessfully() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertThat(user.getActive()).isFalse();

        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_shouldThrowException_whenUserDoesNotExist() {

        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deactivateUser(999L)).isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).save(any(User.class));
    }
}