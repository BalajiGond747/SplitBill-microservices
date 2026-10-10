package com.splitbill.groupservice.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddParticipantRequest {

    @NotNull(message = "User id is required")
    private Long userId;
}