package com.broadcns.msauthserver.service;

import com.broadcns.msauthserver.dto.GitlabProperties;
import com.broadcns.msauthserver.dto.response.GitlabTokenResponse;
import com.broadcns.msauthserver.entity.GitlabUserInfo;
import com.broadcns.msauthserver.entity.Role;
import com.broadcns.msauthserver.entity.User;
import com.broadcns.msauthserver.exception.GitlabAuthenticationException;
import com.broadcns.msauthserver.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class GitLabService {

    private final RestClient restClient;
    private final GitlabProperties gitlabProperties;
    private final UserRepository userRepository;

    private static final String TOKEN_ENDPOINT = "/oauth/token";
    private static final String USER_INFO_ENDPOINT = "/api/v4/user";

    public GitlabUserInfo validateCodeAndGetUserInfo(String code) {
        GitlabTokenResponse tokenResponse = getToken(code);
        return getUserInfo(tokenResponse.getAccessToken());
    }

    private GitlabTokenResponse getToken(String code) {
        try {
            return restClient.post()
                    .uri(TOKEN_ENDPOINT)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(createTokenRequestBody(code))
                    .retrieve()
                    .body(GitlabTokenResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to get token from GitLab", e);
            throw new GitlabAuthenticationException("Failed to get token from GitLab", e);
        }
    }

    private MultiValueMap<String, String> createTokenRequestBody(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", gitlabProperties.getClientId());
        body.add("client_secret", gitlabProperties.getClientSecret());
        body.add("code", code);
        body.add("grant_type", "authorization_code");
        body.add("redirect_uri", gitlabProperties.getRedirectUri());
        return body;
    }

    private GitlabUserInfo getUserInfo(String accessToken) {
        try {
            return restClient.get()
                    .uri(USER_INFO_ENDPOINT)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .retrieve()
                    .body(GitlabUserInfo.class);
        } catch (RestClientException e) {
            log.error("Failed to get user info from GitLab", e);
            throw new GitlabAuthenticationException("Failed to get user info from GitLab", e);
        }
    }

    @Transactional
    public User getOrCreateUser(GitlabUserInfo userInfo) {
        return userRepository.findByGitlabId(userInfo.getId())
                .map(user -> updateUser(user, userInfo))
                .orElseGet(() -> createUser(userInfo));
    }

    private User updateUser(User user, GitlabUserInfo userInfo) {
        user.setEmail(userInfo.getEmail());
        user.setName(userInfo.getName());
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private User createUser(GitlabUserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setName(userInfo.getName());
        user.setGitlabId(userInfo.getId());
        user.setRoles(Set.of(Role.ROLE_USER));
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
