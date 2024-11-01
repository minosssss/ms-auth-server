package com.broadcns.msauthserver.exception;

public class GitlabAuthenticationException extends AuthenticationException {
    public GitlabAuthenticationException(String message) {
        super(message);
    }

    public GitlabAuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}