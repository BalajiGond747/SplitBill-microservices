package com.splitbill.groupservice.service.impl;

import com.splitbill.groupservice.dto.request.GroupCreateRequest;
import com.splitbill.groupservice.dto.request.GroupUpdateRequest;
import com.splitbill.groupservice.dto.response.GroupResponse;
import com.splitbill.groupservice.dto.response.PageResponse;
import com.splitbill.groupservice.entity.Group;
import com.splitbill.groupservice.exception.ResourceNotFoundException;
import com.splitbill.groupservice.mapper.GroupMapper;
import com.splitbill.groupservice.repository.GroupRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceImplTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMapper groupMapper;

    @InjectMocks
    private GroupServiceImpl groupService;

    private Group group;
    private GroupResponse groupResponse;

    @BeforeEach
    void setUp() {

        group = Group.builder()
                .id(1L)
                .name("Goa Trip")
                .description("Expenses for Goa trip")
                .createdBy(1L)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        groupResponse = new GroupResponse(1L, "Goa Trip", "Expenses for Goa trip", 1L, true, group.getCreatedAt(), group.getUpdatedAt());
    }

    @Test
    void createGroup_shouldCreateAndReturnGroup() {

        GroupCreateRequest request = new GroupCreateRequest();
        request.setName("Goa Trip");
        request.setDescription("Expenses for Goa trip");
        request.setCreatedBy(1L);

        when(groupMapper.toEntity(request)).thenReturn(group);

        when(groupRepository.save(group)).thenReturn(group);

        when(groupMapper.toResponse(group)).thenReturn(groupResponse);

        GroupResponse result = groupService.createGroup(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Goa Trip", result.getName());
        assertEquals(1L, result.getCreatedBy());
        assertTrue(result.getActive());

        verify(groupMapper).toEntity(request);
        verify(groupRepository).save(group);
        verify(groupMapper).toResponse(group);
    }

    @Test
    void getGroupById_shouldReturnGroup() {

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        when(groupMapper.toResponse(group)).thenReturn(groupResponse);

        GroupResponse result = groupService.getGroupById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Goa Trip", result.getName());

        verify(groupRepository).findById(1L);
        verify(groupMapper).toResponse(group);
    }

    @Test
    void getGroupById_shouldThrowExceptionWhenGroupDoesNotExist() {

        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> groupService.getGroupById(999L));

        assertEquals("Group not found with id: 999", exception.getMessage());

        verify(groupRepository).findById(999L);
        verify(groupMapper, never()).toResponse(any());
    }

    @Test
    void getAllGroups_shouldReturnPaginatedGroups() {

        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Group> page = new PageImpl<>(List.of(group), pageable, 1);

        when(groupRepository.findAll(any(Pageable.class))).thenReturn(page);

        when(groupMapper.toResponse(group)).thenReturn(groupResponse);

        PageResponse<GroupResponse> result = groupService.getAllGroups(0, 10, "createdAt", "DESC");

        assertNotNull(result);
        assertEquals(1, result.getContent()
                .size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getTotalPages());
        assertTrue(result.isFirst());
        assertTrue(result.isLast());

        verify(groupRepository).findAll(any(Pageable.class));
        verify(groupMapper).toResponse(group);
    }

    @Test
    void searchGroups_shouldReturnMatchingGroups() {

        Page<Group> page = new PageImpl<>(List.of(group));

        when(groupRepository.findByNameContainingIgnoreCase(eq("Goa"), any(Pageable.class))).thenReturn(page);

        when(groupMapper.toResponse(group)).thenReturn(groupResponse);

        PageResponse<GroupResponse> result = groupService.searchGroups("Goa", 0, 10, "name", "ASC");

        assertNotNull(result);
        assertEquals(1, result.getContent()
                .size());
        assertEquals("Goa Trip", result.getContent()
                .get(0)
                .getName());

        verify(groupRepository).findByNameContainingIgnoreCase(eq("Goa"), any(Pageable.class));
    }

    @Test
    void getGroupsByCreatedBy_shouldReturnGroups() {

        Page<Group> page = new PageImpl<>(List.of(group));

        when(groupRepository.findByCreatedBy(eq(1L), any(Pageable.class))).thenReturn(page);

        when(groupMapper.toResponse(group)).thenReturn(groupResponse);

        PageResponse<GroupResponse> result = groupService.getGroupsByCreatedBy(1L, 0, 10, "createdAt", "DESC");

        assertNotNull(result);
        assertEquals(1, result.getContent()
                .size());
        assertEquals(1L, result.getContent()
                .get(0)
                .getCreatedBy());

        verify(groupRepository).findByCreatedBy(eq(1L), any(Pageable.class));
    }

    @Test
    void getActiveGroups_shouldReturnActiveGroups() {

        Page<Group> page = new PageImpl<>(List.of(group));

        when(groupRepository.findByActive(eq(true), any(Pageable.class))).thenReturn(page);

        when(groupMapper.toResponse(group)).thenReturn(groupResponse);

        PageResponse<GroupResponse> result = groupService.getActiveGroups(0, 10, "createdAt", "DESC");

        assertNotNull(result);
        assertEquals(1, result.getContent()
                .size());
        assertTrue(result.getContent()
                .get(0)
                .getActive());

        verify(groupRepository).findByActive(eq(true), any(Pageable.class));
    }

    @Test
    void updateGroup_shouldUpdateAndReturnGroup() {

        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setName("Goa Trip Updated");
        request.setDescription("Updated description");

        Group updatedGroup = Group.builder()
                .id(1L)
                .name("Goa Trip Updated")
                .description("Updated description")
                .createdBy(1L)
                .active(true)
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .build();

        GroupResponse updatedResponse = new GroupResponse(1L, "Goa Trip Updated", "Updated description", 1L, true, group.getCreatedAt(), group.getUpdatedAt());

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        when(groupRepository.save(group)).thenReturn(updatedGroup);

        when(groupMapper.toResponse(updatedGroup)).thenReturn(updatedResponse);

        GroupResponse result = groupService.updateGroup(1L, request);

        assertNotNull(result);
        assertEquals("Goa Trip Updated", result.getName());
        assertEquals("Updated description", result.getDescription());

        verify(groupRepository).findById(1L);
        verify(groupMapper).updateEntity(group, request);
        verify(groupRepository).save(group);
        verify(groupMapper).toResponse(updatedGroup);
    }

    @Test
    void updateGroup_shouldThrowExceptionWhenGroupDoesNotExist() {

        GroupUpdateRequest request = new GroupUpdateRequest();
        request.setName("Updated");
        request.setDescription("Updated description");

        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.updateGroup(999L, request));

        verify(groupRepository).findById(999L);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void deactivateGroup_shouldSetActiveFalse() {

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        when(groupRepository.save(group)).thenReturn(group);

        groupService.deactivateGroup(1L);

        assertFalse(group.getActive());

        verify(groupRepository).findById(1L);
        verify(groupRepository).save(group);
    }

    @Test
    void activateGroup_shouldSetActiveTrue() {

        group.setActive(false);

        when(groupRepository.findById(1L)).thenReturn(Optional.of(group));

        when(groupRepository.save(group)).thenReturn(group);

        groupService.activateGroup(1L);

        assertTrue(group.getActive());

        verify(groupRepository).findById(1L);
        verify(groupRepository).save(group);
    }

    @Test
    void deactivateGroup_shouldThrowExceptionWhenGroupDoesNotExist() {

        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.deactivateGroup(999L));

        verify(groupRepository).findById(999L);
        verify(groupRepository, never()).save(any());
    }

    @Test
    void activateGroup_shouldThrowExceptionWhenGroupDoesNotExist() {

        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> groupService.activateGroup(999L));

        verify(groupRepository).findById(999L);
        verify(groupRepository, never()).save(any());
    }
}