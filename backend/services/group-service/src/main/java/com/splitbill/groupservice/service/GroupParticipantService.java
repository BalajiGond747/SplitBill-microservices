package com.splitbill.groupservice.service;

import com.splitbill.groupservice.dto.request.AddParticipantRequest;
import com.splitbill.groupservice.dto.response.GroupParticipantResponse;

import java.util.List;

public interface GroupParticipantService {

    GroupParticipantResponse addParticipant(Long groupId, AddParticipantRequest request);

    List<GroupParticipantResponse> getActiveParticipants(Long groupId);

    void deactivateParticipant(Long groupId, Long userId);

    void activateParticipant(Long groupId, Long userId);
}