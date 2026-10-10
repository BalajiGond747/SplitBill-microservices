package com.splitbill.groupservice.service.impl;

import com.splitbill.groupservice.dto.request.AddParticipantRequest;
import com.splitbill.groupservice.dto.response.GroupParticipantResponse;
import com.splitbill.groupservice.entity.GroupParticipant;
import com.splitbill.groupservice.exception.DuplicateResourceException;
import com.splitbill.groupservice.exception.ResourceNotFoundException;
import com.splitbill.groupservice.repository.GroupParticipantRepository;
import com.splitbill.groupservice.repository.GroupRepository;
import com.splitbill.groupservice.service.GroupParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupParticipantServiceImpl implements GroupParticipantService {

    private final GroupRepository groupRepository;
    private final GroupParticipantRepository participantRepository;

    @Override
    @Transactional
    public GroupParticipantResponse addParticipant(Long groupId, AddParticipantRequest request) {

        validateActiveGroup(groupId);

        Long userId = request.getUserId();

        var existingParticipant = participantRepository.findByGroupIdAndUserId(groupId, userId);

        if (existingParticipant.isPresent()) {

            GroupParticipant participant = existingParticipant.get();

            if (Boolean.TRUE.equals(participant.getActive())) {
                throw new DuplicateResourceException("User is already a member of this group");
            }

            participant.setActive(true);

            return toResponse(participantRepository.save(participant));
        }

        GroupParticipant participant = GroupParticipant.builder()
                .groupId(groupId)
                .userId(userId)
                .active(true)
                .build();

        return toResponse(participantRepository.save(participant));
    }

    @Override
    public List<GroupParticipantResponse> getActiveParticipants(Long groupId) {

        validateGroup(groupId);

        return participantRepository.findByGroupIdAndActiveTrue(groupId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deactivateParticipant(Long groupId, Long userId) {

        GroupParticipant participant = findParticipant(groupId, userId);

        participant.setActive(false);

        participantRepository.save(participant);
    }

    @Override
    @Transactional
    public void activateParticipant(Long groupId, Long userId) {

        validateActiveGroup(groupId);

        GroupParticipant participant = findParticipant(groupId, userId);

        participant.setActive(true);

        participantRepository.save(participant);
    }

    private void validateGroup(Long groupId) {

        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("Group not found with id: " + groupId);
        }
    }

    private void validateActiveGroup(Long groupId) {

        var group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + groupId));

        if (!Boolean.TRUE.equals(group.getActive())) {
            throw new ResourceNotFoundException("Group is inactive");
        }
    }

    private GroupParticipant findParticipant(Long groupId, Long userId) {

        return participantRepository.findByGroupIdAndUserId(groupId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this group"));
    }

    private GroupParticipantResponse toResponse(GroupParticipant participant) {

        GroupParticipantResponse response = new GroupParticipantResponse();

        response.setId(participant.getId());
        response.setGroupId(participant.getGroupId());
        response.setUserId(participant.getUserId());
        response.setActive(participant.getActive());
        response.setCreatedAt(participant.getCreatedAt());
        response.setUpdatedAt(participant.getUpdatedAt());

        return response;
    }
}
