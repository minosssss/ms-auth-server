package com.broadcns.msauthserver.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.aspectj.bridge.IMessage;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    private String message;
    private String email;
    private String name;

}