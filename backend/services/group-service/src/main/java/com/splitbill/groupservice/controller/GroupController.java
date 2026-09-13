package com.splitbill.groupservice.controller;

import com.splitbill.groupservice.dto.request.GroupCreateRequest;
import com.splitbill.groupservice.dto.request.GroupUpdateRequest;
import com.splitbill.groupservice.dto.response.ApiResponse;
import com.splitbill.groupservice.dto.response.GroupResponse;
import com.splitbill.groupservice.dto.response.PageResponse;
import com.splitbill.groupservice.service.GroupService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/groups")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupResponse>> createGroup(@Valid @RequestBody GroupCreateRequest request) {

        GroupResponse response = groupService.createGroup(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Group created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> getGroupById(@PathVariable @NotNull(message = "Group id is required") Long id) {

        GroupResponse response = groupService.getGroupById(id);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GroupResponse>>> getAllGroups(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must not exceed 100") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PageResponse<GroupResponse> response = groupService.getAllGroups(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<GroupResponse>>> searchGroups(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must not exceed 100") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PageResponse<GroupResponse> response = groupService.searchGroups(name, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/created-by/{createdBy}")
    public ResponseEntity<ApiResponse<PageResponse<GroupResponse>>> getGroupsByCreatedBy(
            @PathVariable @NotNull(message = "Created by is required") Long createdBy,
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must not exceed 100") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PageResponse<GroupResponse> response = groupService.getGroupsByCreatedBy(createdBy, page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PageResponse<GroupResponse>>> getActiveGroups(
            @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page must be greater than or equal to 0") int page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "Size must be greater than 0") @Max(value = 100, message = "Size must not exceed 100") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection) {

        PageResponse<GroupResponse> response = groupService.getActiveGroups(page, size, sortBy, sortDirection);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GroupResponse>> updateGroup(
            @PathVariable @NotNull(message = "Group id is required") Long id,
            @Valid @RequestBody GroupUpdateRequest request) {

        GroupResponse response = groupService.updateGroup(id, request);

        return ResponseEntity.ok(ApiResponse.success("Group updated successfully", response));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateGroup(@PathVariable @NotNull(message = "Group id is required") Long id) {

        groupService.deactivateGroup(id);

        return ResponseEntity.ok(ApiResponse.success("Group deactivated successfully", null));
    }

    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<Void>> activateGroup(@PathVariable @NotNull(message = "Group id is required") Long id) {

        groupService.activateGroup(id);

        return ResponseEntity.ok(ApiResponse.success("Group activated successfully", null));
    }
}