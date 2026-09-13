package com.splitbill.groupservice.controller;

import com.splitbill.groupservice.dto.response.GroupResponse;
import com.splitbill.groupservice.dto.response.PageResponse;
import com.splitbill.groupservice.exception.ResourceNotFoundException;
import com.splitbill.groupservice.service.GroupService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = GroupController.class)
class GroupControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GroupService groupService;

    private GroupResponse createGroupResponse() {

        return new GroupResponse(1L, "Goa Trip", "Expenses for Goa trip", 1L, true, LocalDateTime.now(), LocalDateTime.now());
    }

    @Test
    void createGroup_shouldReturn201() throws Exception {

        GroupResponse response = createGroupResponse();

        when(groupService.createGroup(any())).thenReturn(response);

        String requestBody = """
                {
                    "name": "Goa Trip",
                    "description": "Expenses for Goa trip",
                    "createdBy": 1
                }
                """;

        mockMvc.perform(post("/api/v1/groups").contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Group created successfully"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Goa Trip"));

        verify(groupService).createGroup(any());
    }

    @Test
    void createGroup_withInvalidData_shouldReturn400() throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "description": "Test"
                }
                """;

        mockMvc.perform(post("/api/v1/groups").contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors.name").value("Group name is required"))
                .andExpect(jsonPath("$.validationErrors.createdBy").value("Created by is required"));

        verifyNoInteractions(groupService);
    }

    @Test
    void getGroupById_shouldReturn200() throws Exception {

        GroupResponse response = createGroupResponse();

        when(groupService.getGroupById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/groups/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("Goa Trip"));

        verify(groupService).getGroupById(1L);
    }

    @Test
    void getGroupById_whenNotFound_shouldReturn404() throws Exception {

        when(groupService.getGroupById(999L)).thenThrow(new ResourceNotFoundException("Group not found with id: 999"));

        mockMvc.perform(get("/api/v1/groups/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Group not found with id: 999"));

        verify(groupService).getGroupById(999L);
    }

    @Test
    void getAllGroups_shouldReturnPaginatedResponse() throws Exception {

        GroupResponse response = createGroupResponse();

        PageResponse<GroupResponse> pageResponse = new PageResponse<>(List.of(response), 0, 10, 1, 1, true, true);

        when(groupService.getAllGroups(0, 10, "createdAt", "DESC")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/groups"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(10))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1))
                .andExpect(jsonPath("$.data.first").value(true))
                .andExpect(jsonPath("$.data.last").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Goa Trip"));

        verify(groupService).getAllGroups(0, 10, "createdAt", "DESC");
    }

    @Test
    void getAllGroups_withInvalidPage_shouldReturn400() throws Exception {

        mockMvc.perform(get("/api/v1/groups").param("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(groupService);
    }

    @Test
    void getAllGroups_withZeroSize_shouldReturn400() throws Exception {

        mockMvc.perform(get("/api/v1/groups").param("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(groupService);
    }

    @Test
    void getAllGroups_withSizeAbove100_shouldReturn400() throws Exception {

        mockMvc.perform(get("/api/v1/groups").param("size", "101"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(groupService);
    }

    @Test
    void searchGroups_shouldReturnResults() throws Exception {

        GroupResponse response = createGroupResponse();

        PageResponse<GroupResponse> pageResponse = new PageResponse<>(List.of(response), 0, 10, 1, 1, true, true);

        when(groupService.searchGroups(eq("Goa"), eq(0), eq(10), eq("createdAt"), eq("DESC"))).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/groups/search").param("name", "Goa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].name").value("Goa Trip"));

        verify(groupService).searchGroups("Goa", 0, 10, "createdAt", "DESC");
    }

    @Test
    void getGroupsByCreatedBy_shouldReturnResults() throws Exception {

        GroupResponse response = createGroupResponse();

        PageResponse<GroupResponse> pageResponse = new PageResponse<>(List.of(response), 0, 10, 1, 1, true, true);

        when(groupService.getGroupsByCreatedBy(1L, 0, 10, "createdAt", "DESC")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/groups/created-by/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].createdBy").value(1));

        verify(groupService).getGroupsByCreatedBy(1L, 0, 10, "createdAt", "DESC");
    }

    @Test
    void getActiveGroups_shouldReturnResults() throws Exception {

        GroupResponse response = createGroupResponse();

        PageResponse<GroupResponse> pageResponse = new PageResponse<>(List.of(response), 0, 10, 1, 1, true, true);

        when(groupService.getActiveGroups(0, 10, "createdAt", "DESC")).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/groups/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].active").value(true));

        verify(groupService).getActiveGroups(0, 10, "createdAt", "DESC");
    }

    @Test
    void updateGroup_shouldReturn200() throws Exception {

        GroupResponse response = createGroupResponse();

        response.setName("Goa Trip Updated");

        when(groupService.updateGroup(eq(1L), any())).thenReturn(response);

        String requestBody = """
                {
                    "name": "Goa Trip Updated",
                    "description": "Updated description"
                }
                """;

        mockMvc.perform(put("/api/v1/groups/1").contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Goa Trip Updated"));

        verify(groupService).updateGroup(eq(1L), any());
    }

    @Test
    void updateGroup_withInvalidData_shouldReturn400() throws Exception {

        String requestBody = """
                {
                    "name": "",
                    "description": "Invalid"
                }
                """;

        mockMvc.perform(put("/api/v1/groups/1").contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(groupService);
    }

    @Test
    void deactivateGroup_shouldReturn200() throws Exception {

        doNothing().when(groupService)
                .deactivateGroup(1L);

        mockMvc.perform(patch("/api/v1/groups/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Group deactivated successfully"));

        verify(groupService).deactivateGroup(1L);
    }

    @Test
    void activateGroup_shouldReturn200() throws Exception {

        doNothing().when(groupService)
                .activateGroup(1L);

        mockMvc.perform(patch("/api/v1/groups/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Group activated successfully"));

        verify(groupService).activateGroup(1L);
    }
}