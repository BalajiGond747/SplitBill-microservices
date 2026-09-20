package com.splitbill.settlementservice.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private boolean success;

    private String message;

    private LocalDateTime timestamp;

    private Map<String, String> errors;
}