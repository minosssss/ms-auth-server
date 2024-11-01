package com.broadcns.msauthserver.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GitLabOAuth2TokenProvider {

    @Value("${gitlab.base-url}")
    private String GITLAB_BASE_URL;

    @Value("${gitlab.client-id}")
    private String GITLAB_CLIENT_ID;

    @Value("${gitlab.client-secret}")
    private String GITLAB_CLIENT_SECRET;



    private final RestTemplate restTemplate;

    public GitLabOAuth2TokenProvider() {
        restTemplate = new RestTemplate();
    }



    public String getUserNameFromToken(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GITLAB_BASE_URL + "/oauth/userinfo",
                HttpMethod.GET,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }

        throw new RuntimeException("GitLab UserInfo 요청 실패");
    }

    public String refreshAccessToken(String refreshToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = "grant_type=refresh_token" +
                "&refresh_token=" + refreshToken +
                "&client_id=" + GITLAB_CLIENT_ID +
                "&client_secret=" + GITLAB_CLIENT_SECRET;

        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                GITLAB_BASE_URL + "/oauth/token",
                HttpMethod.POST,
                entity,
                String.class
        );

        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        }

        throw new RuntimeException("GitLab Access Token 재발급 실패");
    }
}