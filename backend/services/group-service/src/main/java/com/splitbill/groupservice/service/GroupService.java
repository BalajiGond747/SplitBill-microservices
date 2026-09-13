package com.splitbill.groupservice.service;

import com.splitbill.groupservice.dto.request.GroupCreateRequest;
import com.splitbill.groupservice.dto.request.GroupUpdateRequest;
import com.splitbill.groupservice.dto.response.GroupResponse;
import com.splitbill.groupservice.dto.response.PageResponse;

public interface GroupService {

    GroupResponse createGroup(GroupCreateRequest request);

    GroupResponse getGroupById(Long id);

    PageResponse<GroupResponse> getAllGroups(int page, int size, String sortBy, String sortDirection);

    PageResponse<GroupResponse> searchGroups(String name, int page, int size, String sortBy, String sortDirection);

    PageResponse<GroupResponse> getGroupsByCreatedBy(Long createdBy, int page, int size, String sortBy, String sortDirection);

    PageResponse<GroupResponse> getActiveGroups(int page, int size, String sortBy, String sortDirection);

    GroupResponse updateGroup(Long id, GroupUpdateRequest request);

    void deactivateGroup(Long id);

    void activateGroup(Long id);
}