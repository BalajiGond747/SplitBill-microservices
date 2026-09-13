package com.splitbill.groupservice.service.impl;

import com.splitbill.groupservice.dto.request.GroupCreateRequest;
import com.splitbill.groupservice.dto.request.GroupUpdateRequest;
import com.splitbill.groupservice.dto.response.GroupResponse;
import com.splitbill.groupservice.dto.response.PageResponse;
import com.splitbill.groupservice.entity.Group;
import com.splitbill.groupservice.exception.ResourceNotFoundException;
import com.splitbill.groupservice.mapper.GroupMapper;
import com.splitbill.groupservice.repository.GroupRepository;
import com.splitbill.groupservice.service.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GroupServiceImpl implements GroupService {

    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;

    @Override
    @Transactional
    public GroupResponse createGroup(GroupCreateRequest request) {
        Group group = groupMapper.toEntity(request);

        Group savedGroup = groupRepository.save(group);

        return groupMapper.toResponse(savedGroup);
    }

    @Override
    public GroupResponse getGroupById(Long id) {
        Group group = findGroupById(id);

        return groupMapper.toResponse(group);
    }

    @Override
    public PageResponse<GroupResponse> getAllGroups(int page, int size, String sortBy, String sortDirection) {

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        Page<Group> groupPage = groupRepository.findAll(pageable);

        return toPageResponse(groupPage);
    }

    @Override
    public PageResponse<GroupResponse> searchGroups(String name, int page, int size, String sortBy, String sortDirection) {

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        Page<Group> groupPage = groupRepository.findByNameContainingIgnoreCase(name, pageable);

        return toPageResponse(groupPage);
    }

    @Override
    public PageResponse<GroupResponse> getGroupsByCreatedBy(Long createdBy, int page, int size, String sortBy, String sortDirection) {

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        Page<Group> groupPage = groupRepository.findByCreatedBy(createdBy, pageable);

        return toPageResponse(groupPage);
    }

    @Override
    public PageResponse<GroupResponse> getActiveGroups(int page, int size, String sortBy, String sortDirection) {

        Pageable pageable = createPageable(page, size, sortBy, sortDirection);

        Page<Group> groupPage = groupRepository.findByActive(true, pageable);

        return toPageResponse(groupPage);
    }

    @Override
    @Transactional
    public GroupResponse updateGroup(Long id, GroupUpdateRequest request) {

        Group group = findGroupById(id);

        groupMapper.updateEntity(group, request);

        Group updatedGroup = groupRepository.save(group);

        return groupMapper.toResponse(updatedGroup);
    }

    @Override
    @Transactional
    public void deactivateGroup(Long id) {
        Group group = findGroupById(id);

        group.setActive(false);

        groupRepository.save(group);
    }

    @Override
    @Transactional
    public void activateGroup(Long id) {
        Group group = findGroupById(id);

        group.setActive(true);

        groupRepository.save(group);
    }

    private Group findGroupById(Long id) {
        return groupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found with id: " + id));
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {

        Sort.Direction direction = Sort.Direction.fromString(sortDirection);

        Sort sort = Sort.by(direction, sortBy);

        return PageRequest.of(page, size, sort);
    }

    private PageResponse<GroupResponse> toPageResponse(Page<Group> groupPage) {

        return new PageResponse<>(groupPage.getContent()
                .stream()
                .map(groupMapper::toResponse)
                .toList(), groupPage.getNumber(), groupPage.getSize(), groupPage.getTotalElements(), groupPage.getTotalPages(), groupPage.isFirst(), groupPage.isLast());
    }
}