package com.broadcns.msauthserver.jwt;


import com.broadcns.msauthserver.dto.response.LoginResponse;
import com.broadcns.msauthserver.entity.GitlabUserInfo;
import com.broadcns.msauthserver.entity.User;
import com.broadcns.msauthserver.exception.AuthenticationException;
import com.broadcns.msauthserver.exception.GitlabAuthenticationException;
import com.broadcns.msauthserver.exception.InvalidTokenException;
import com.broadcns.msauthserver.service.GitLabService;
import com.broadcns.msauthserver.utils.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.ErrorResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GitlabAuthenticationFilter extends OncePerRequestFilter {

    private final GitLabService gitlabService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!request.getRequestURI().equals("/api/auth/gitlab/login") ||
                !request.getMethod().equals("POST")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new GitlabAuthenticationException("No GitLab code provided");
            }

            String gitlabCode = authHeader.substring(7);
            GitlabUserInfo userInfo = gitlabService.validateCodeAndGetUserInfo(gitlabCode);
            User user = gitlabService.getOrCreateUser(userInfo);

            // JWT 토큰 생성 및 인증 처리
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.name()))
                            .collect(Collectors.toSet())
            );

            String accessToken = jwtTokenProvider.createAccessToken(authentication);
            String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

            // 쿠키에 토큰 저장
            CookieUtil.addCookie(response, "access_token", accessToken,
                    accessTokenValidity);
            CookieUtil.addCookie(response, "refresh_token", refreshToken,
                   refreshTokenValidity);

            // 응답 생성
            LoginResponse loginResponse = new LoginResponse(
                    "Login successful",
                    user.getEmail(),
                    user.getName()
            );

            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            objectMapper.writeValue(response.getOutputStream(), loginResponse);

        } catch (GitlabAuthenticationException e) {
            handleAuthenticationFailure(response, e);
        } catch (Exception e) {
            log.error("Authentication failed", e);
            handleAuthenticationFailure(response,
                    new GitlabAuthenticationException("Authentication failed"));
        }
    }

    private void handleAuthenticationFailure(HttpServletResponse response,
                                             AuthenticationException e)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        Map<String, String> error = new HashMap<>();
        error.put("error", "Authentication failed");
        error.put("message", e.getMessage());

        objectMapper.writeValue(response.getOutputStream(), error);
    }
}