package com.broadcns.msauthserver.integration;

import com.broadcns.msauthserver.exception.GitlabAuthenticationException;
import com.broadcns.msauthserver.service.GitLabService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitLabService gitlabService;

    @Test
    void handleGitlabAuthenticationException() throws Exception {
        when(gitlabService.validateCodeAndGetUserInfo(anyString()))
                .thenThrow(new GitlabAuthenticationException("Invalid GitLab code"));

        mockMvc.perform(post("/api/auth/gitlab/login")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-code"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid GitLab code"))
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void handleInvalidTokenException() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie("refresh_token", "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void handleMethodArgumentNotValidException() throws Exception {
        // 유효하지 않은 요청 바디로 테스트
        mockMvc.perform(post("/api/users/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").exists());
    }
}