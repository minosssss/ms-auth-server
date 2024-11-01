package com.broadcns.msauthserver.integration;

import com.broadcns.msauthserver.data.UserData;
import com.broadcns.msauthserver.dto.JwtProperties;
import com.broadcns.msauthserver.entity.User;
import com.broadcns.msauthserver.exception.GitlabAuthenticationException;
import com.broadcns.msauthserver.jwt.JwtTokenProvider;
import com.broadcns.msauthserver.repository.UserRepository;
import com.broadcns.msauthserver.service.GitLabService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TokenExpiredTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @SpyBean
    private JwtProperties jwtProperties;


    @Test
    void accessToken_ExpiresAndRefreshes() throws Exception {
        // Given: 짧은 유효기간의 액세스 토큰 설정
        when(jwtProperties.getAccessTokenValidity()).thenReturn(1L); // 1초
        User user = UserData.createUser();
        userRepository.save(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );

        String accessToken = jwtTokenProvider.createAccessToken(auth);
        String refreshToken = jwtTokenProvider.createRefreshToken(auth);

        // When: 토큰이 만료되기를 기다림
        Thread.sleep(1100);

        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        // Then: 만료된 액세스 토큰으로 요청 시 자동으로 리프레시
        mockMvc.perform(get("/api/auth/me")
                        .cookie(accessTokenCookie, refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andDo(print());
    }

    @Test
    void bothTokens_Expired() throws Exception {
        // Given: 만료된 토큰들
        when(jwtProperties.getAccessTokenValidity()).thenReturn(1L);
        when(jwtProperties.getRefreshTokenValidity()).thenReturn(1L);

        User user = UserData.createUser();
        userRepository.save(user);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                user.getEmail(),
                null,
                user.getRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );

        String accessToken = jwtTokenProvider.createAccessToken(auth);
        String refreshToken = jwtTokenProvider.createRefreshToken(auth);

        // When: 토큰들이 만료되기를 기다림
        Thread.sleep(3100);

        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);

        // Then: 모든 토큰이 만료되어 인증 실패
        mockMvc.perform(get("/api/me")
                        .cookie(accessTokenCookie, refreshTokenCookie))
                .andExpect(status().isUnauthorized())
                .andDo(print());
    }
}