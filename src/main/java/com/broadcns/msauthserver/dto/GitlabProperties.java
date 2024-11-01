package com.broadcns.msauthserver.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@ConfigurationProperties(prefix = "gitlab")
@Component
public class GitlabProperties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
}