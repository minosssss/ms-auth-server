package com.broadcns.msauthserver.exception;

public class InvalidTokenException extends AuthenticationException  {
    public InvalidTokenException(String message) {
        super(message);
    }
}