package com.broadcns.msauthserver.integration;

import com.broadcns.msauthserver.data.UserData;
import com.broadcns.msauthserver.entity.User;
import com.broadcns.msauthserver.jwt.JwtTokenProvider;
import com.broadcns.msauthserver.repository.UserRepository;
import com.broadcns.msauthserver.service.GitLabService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private GitLabService gitlabService;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }


    @Test
    void refreshToken_Success() throws Exception {
        // Given
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
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);

        // When & Then
        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(accessTokenCookie, refreshTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().exists("access_token"))
                .andExpect(cookie().exists("refresh_token"))
                .andDo(print());
    }

    @Test
    void getCurrentUser_Success() throws Exception {
        // Given
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
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);

        // When & Then
        mockMvc.perform(get("/api/auth/me")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.name").value(user.getName()))
                .andDo(print());
    }

    @Test
    void logout_Success() throws Exception {
        // Given
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
        Cookie accessTokenCookie = new Cookie("access_token", accessToken);

        // When & Then
        mockMvc.perform(post("/api/auth/logout")
                        .cookie(accessTokenCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("access_token", 0))
                .andDo(print());
    }
}