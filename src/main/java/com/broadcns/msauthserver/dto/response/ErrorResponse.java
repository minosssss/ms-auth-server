package com.broadcns.msauthserver.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@Builder
public class ErrorResponse {
    private final int status;
    private final String message;
    private final String path;
    private final LocalDateTime timestamp;
    private Map<String, String> errors;

}