package com.splitbill.groupservice.mapper;

import com.splitbill.groupservice.dto.request.GroupCreateRequest;
import com.splitbill.groupservice.dto.request.GroupUpdateRequest;
import com.splitbill.groupservice.dto.response.GroupResponse;
import com.splitbill.groupservice.entity.Group;
import org.springframework.stereotype.Component;

@Component
public class GroupMapper {

    public Group toEntity(GroupCreateRequest request) {
        return Group.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(request.getCreatedBy())
                .active(true)
                .build();
    }

    public void updateEntity(Group group, GroupUpdateRequest request) {
        group.setName(request.getName());
        group.setDescription(request.getDescription());
    }

    public GroupResponse toResponse(Group group) {
        GroupResponse response = new GroupResponse();

        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setCreatedBy(group.getCreatedBy());
        response.setActive(group.getActive());
        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());

        return response;
    }
}