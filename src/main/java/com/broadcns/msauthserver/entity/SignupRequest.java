package com.broadcns.msauthserver.entity;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SignupRequest {

    private String email;
    private String password;
    private String userName;
}
