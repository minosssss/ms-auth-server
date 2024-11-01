package com.broadcns.msauthserver.jwt;

import com.broadcns.msauthserver.dto.JwtProperties;
import com.broadcns.msauthserver.dto.response.ErrorResponse;
import com.broadcns.msauthserver.exception.InvalidTokenException;
import com.broadcns.msauthserver.utils.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

import static com.broadcns.msauthserver.dto.response.ErrorResponse.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;


    @Override
    public void doFilterInternal(HttpServletRequest request,
                                 HttpServletResponse response,
                                 FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String accessToken = CookieUtil.getCookie(request, "access_token").orElse(null);
            String refreshToken = CookieUtil.getCookie(request, "refresh_token").orElse(null);

            if (accessToken != null) {
                if (jwtTokenProvider.validateToken(accessToken, false)) {
                    Authentication authentication = jwtTokenProvider.getAuthentication(accessToken);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                    if (jwtTokenProvider.isTokenExpiringSoon(accessToken)) {
                        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
                        CookieUtil.addCookie(response, "access_token", newAccessToken, jwtProperties.getAccessTokenValidity());
                    }

                }
            }

        } catch (InvalidTokenException e) {
            handleExpiredToken(request, response);
        } catch (Exception e) {
            log.error("JWT Authentication failed: {}", e.getMessage());
        }
        filterChain.doFilter(request, response);
    }


    private void handleExpiredToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = CookieUtil.getCookie(request, "refresh_token").orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

            if (StringUtils.hasText(refreshToken) &&
                    jwtTokenProvider.validateToken(refreshToken, true)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
                String newAccessToken = jwtTokenProvider.createAccessToken(authentication);

                CookieUtil.addCookie(response, "access_token", newAccessToken,
                        jwtProperties.getAccessTokenValidity());
                CookieUtil.addCookie(response, "refresh_token", newAccessToken,
                        jwtProperties.getRefreshTokenValidity());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            log.error("Token refresh failed: {}", e.getMessage());

        }
    }

}
