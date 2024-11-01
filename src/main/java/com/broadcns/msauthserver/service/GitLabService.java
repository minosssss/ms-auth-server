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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Service
public class GitLabService {

    private final RestTemplate restTemplate;
    private final GitlabProperties gitlabProperties;
    private final UserRepository userRepository;

    private static final String GITLAB_TOKEN_URL = "https://gitlab.com/oauth/token";
    private static final String GITLAB_USER_INFO_URL = "https://gitlab.com/api/v4/user";

    public GitlabUserInfo validateCodeAndGetUserInfo(String code) {
        GitlabTokenResponse tokenResponse = exchangeCodeForToken(code);
        return getUserInfo(tokenResponse.getAccessToken());
    }

    private GitlabTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", gitlabProperties.getClientId());
        params.add("client_secret", gitlabProperties.getClientSecret());
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", gitlabProperties.getRedirectUri());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            return restTemplate.postForObject(GITLAB_TOKEN_URL, request, GitlabTokenResponse.class);
        } catch (RestClientException e) {
            log.error("Failed to exchange code for token", e);
            throw new GitlabAuthenticationException("Failed to exchange code for token", e);
        }
    }

    private GitlabUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<GitlabUserInfo> response = restTemplate.exchange(
                    GITLAB_USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    GitlabUserInfo.class
            );
            return response.getBody();
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

    private User createUser(GitlabUserInfo userInfo) {
        User user = new User();
        user.setEmail(userInfo.getEmail());
        user.setName(userInfo.getName());
        user.setGitlabId(userInfo.getId());
        user.setRoles(Set.of(Role.ROLE_USER));
        return userRepository.save(user);
    }

    private User updateUser(User user, GitlabUserInfo userInfo) {
        user.setEmail(userInfo.getEmail());
        user.setName(userInfo.getName());
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
