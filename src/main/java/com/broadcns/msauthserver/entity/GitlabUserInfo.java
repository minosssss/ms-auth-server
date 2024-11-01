package com.broadcns.msauthserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class GitlabUserInfo {
    private String id;
    private String email;
    private String name;
}
