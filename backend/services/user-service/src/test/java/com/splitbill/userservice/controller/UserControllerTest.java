package com.splitbill.userservice.controller;

import com.splitbill.userservice.dto.request.UserCreateRequest;
import com.splitbill.userservice.dto.request.UserUpdateRequest;
import com.splitbill.userservice.dto.response.PageResponse;
import com.splitbill.userservice.dto.response.UserResponse;
import com.splitbill.userservice.exception.DuplicateResourceException;
import com.splitbill.userservice.exception.GlobalExceptionHandler;
import com.splitbill.userservice.exception.ResourceNotFoundException;
import com.splitbill.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_shouldReturn201() throws Exception {

        UserCreateRequest request = new UserCreateRequest();
        request.setName("Balaji Gond");
        request.setEmail("balaji.gond@example.com");
        request.setUsername("balaji");

        UserResponse response = new UserResponse(1L, "Balaji Gond", "balaji.gond@example.com", "balaji", true, null, null);

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Balaji Gond"))
                .andExpect(jsonPath("$.data.email").value("balaji.gond@example.com"))
                .andExpect(jsonPath("$.data.username").value("balaji"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(userService).createUser(any(UserCreateRequest.class));
    }

    @Test
    void getUserById_shouldReturn200() throws Exception {

        UserResponse response = new UserResponse(1L, "Balaji Gond", "balaji.gond@example.com", "balaji", true, null, null);

        when(userService.getUserById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User fetched successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Balaji Gond"))
                .andExpect(jsonPath("$.data.email").value("balaji.gond@example.com"));

        verify(userService).getUserById(1L);
    }

    @Test
    void getUserById_shouldReturn404_whenUserDoesNotExist() throws Exception {

        when(userService.getUserById(999L)).thenThrow(new ResourceNotFoundException("User with id '999' not found"));

        mockMvc.perform(get("/api/v1/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User with id '999' not found"))
                .andExpect(jsonPath("$.path").value("/api/v1/users/999"));
    }

    @Test
    void getUsers_shouldReturnPaginatedUsers() throws Exception {

        UserResponse user1 = new UserResponse(1L, "Balaji Gond", "balaji.gond@example.com", "balaji", true, null, null);

        UserResponse user2 = new UserResponse(2L, "Rahul Sharma", "rahul.sharma@example.com", "rahul", true, null, null);

        PageResponse<UserResponse> pageResponse = new PageResponse<>(List.of(user1, user2), 0, 10, 2, 1, true, true);

        when(userService.getUsers(any(), any(), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/users").param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Users fetched successfully"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(2))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[1].id").value(2))
                .andExpect(jsonPath("$.data.totalElements").value(2))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true));

        verify(userService).getUsers(any(), any(), any());
    }

    @Test
    void getUsers_shouldAcceptSearchAndFilterParameters() throws Exception {

        PageResponse<UserResponse> pageResponse = new PageResponse<>(List.of(), 0, 5, 0, 0, true, true);

        when(userService.getUsers(eq("Balaji"), eq(true), any())).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/users").param("name", "Balaji")
                        .param("active", "true")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sort", "name,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.totalElements").value(0))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(5));

        verify(userService).getUsers(eq("Balaji"), eq(true), any());
    }

    @Test
    void updateUser_shouldReturn200() throws Exception {

        UserUpdateRequest request = new UserUpdateRequest();

        request.setName("Balaji Updated");
        request.setEmail("balaji.updated@example.com");
        request.setUsername("balaji_updated");

        UserResponse response = new UserResponse(1L, "Balaji Updated", "balaji.updated@example.com", "balaji_updated", true, null, null);

        when(userService.updateUser(eq(1L), any(UserUpdateRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/users/1").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User updated successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Balaji Updated"))
                .andExpect(jsonPath("$.data.username").value("balaji_updated"));

        verify(userService).updateUser(eq(1L), any(UserUpdateRequest.class));
    }

    @Test
    void deactivateUser_shouldReturn200() throws Exception {

        doNothing().when(userService)
                .deactivateUser(1L);

        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User deactivated successfully"))
                .andExpect(jsonPath("$.data").doesNotExist());

        verify(userService).deactivateUser(1L);
    }

    @Test
    void createUser_shouldReturn400_whenValidationFails() throws Exception {

        UserCreateRequest request = new UserCreateRequest();

        request.setName("");
        request.setEmail("invalid-email");
        request.setUsername("ab");

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors").exists())
                .andExpect(jsonPath("$.validationErrors.name").value("Name is required"))
                .andExpect(jsonPath("$.validationErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.validationErrors.username").value("Username must be between 3 and 50 characters"));

        verifyNoInteractions(userService);
    }

    @Test
    void createUser_shouldReturn409_whenDuplicateUser() throws Exception {

        UserCreateRequest request = new UserCreateRequest();

        request.setName("Duplicate User");
        request.setEmail("duplicate@example.com");
        request.setUsername("duplicate");

        when(userService.createUser(any(UserCreateRequest.class))).thenThrow(new DuplicateResourceException("User with email 'duplicate@example.com' already exists"));

        mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User with email 'duplicate@example.com' already exists"));
    }
}