package com.splitbill.groupservice.controller;

import com.splitbill.groupservice.dto.request.AddParticipantRequest;
import com.splitbill.groupservice.dto.response.ApiResponse;
import com.splitbill.groupservice.dto.response.GroupParticipantResponse;
import com.splitbill.groupservice.service.GroupParticipantService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/members")
@RequiredArgsConstructor
public class GroupParticipantController {

    private final GroupParticipantService participantService;

    @PostMapping
    public ResponseEntity<ApiResponse<GroupParticipantResponse>> addMember(@PathVariable @NotNull(message = "Group id is required") Long groupId,

                                                                           @Valid @RequestBody AddParticipantRequest request) {

        GroupParticipantResponse response = participantService.addParticipant(groupId, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Member added successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GroupParticipantResponse>>> getMembers(@PathVariable @NotNull(message = "Group id is required") Long groupId) {

        List<GroupParticipantResponse> response = participantService.getActiveParticipants(groupId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{userId}/deactivate")
    public ResponseEntity<ApiResponse<Void>> deactivateMember(@PathVariable Long groupId, @PathVariable Long userId) {

        participantService.deactivateParticipant(groupId, userId);

        return ResponseEntity.ok(ApiResponse.success("Member removed from group", null));
    }

    @PatchMapping("/{userId}/activate")
    public ResponseEntity<ApiResponse<Void>> activateMember(@PathVariable Long groupId, @PathVariable Long userId) {

        participantService.activateParticipant(groupId, userId);

        return ResponseEntity.ok(ApiResponse.success("Member added to group", null));
    }
}