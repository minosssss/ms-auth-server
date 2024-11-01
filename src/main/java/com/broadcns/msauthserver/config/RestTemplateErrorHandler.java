package com.broadcns.msauthserver.config;

import com.broadcns.msauthserver.exception.GitlabAuthenticationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

@Slf4j
public class RestTemplateErrorHandler implements ResponseErrorHandler {

    @Override
    public boolean hasError(ClientHttpResponse response) throws IOException {
        return response.getStatusCode().is4xxClientError() ||
                response.getStatusCode().is5xxServerError();
    }

    @Override
    public void handleError(ClientHttpResponse response) throws IOException {
        if (response.getStatusCode().is4xxClientError()) {
            log.error("Client Error: {}", response.getStatusCode());
            throw new GitlabAuthenticationException(
                    "Failed to authenticate with GitLab: " + response.getStatusCode());
        } else if (response.getStatusCode().is5xxServerError()) {
            log.error("Server Error: {}", response.getStatusCode());
            throw new GitlabAuthenticationException(
                    "GitLab server error: " + response.getStatusCode());
        }
    }
}