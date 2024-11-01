package com.broadcns.msauthserver.security;

import com.broadcns.msauthserver.exception.InvalidTokenException;
import com.broadcns.msauthserver.jwt.JwtAuthenticationFilter;
import com.broadcns.msauthserver.jwt.JwtTokenProvider;
import com.broadcns.msauthserver.utils.TestCookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(jwtTokenProvider);
    }

    @Test
    void doFilterInternal_WithValidToken_SetsAuthentication() throws Exception {
        // Given
        String validToken = "valid-token";
        Cookie accessTokenCookie = TestCookieUtil.createTestCookie("access_token", validToken);
        Authentication mockAuth = mock(Authentication.class);

        when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie});
        when(jwtTokenProvider.validateToken(validToken, false)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(validToken)).thenReturn(mockAuth);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertEquals(mockAuth, auth);
    }

    @Test
    void doFilterInternal_WithExpiredToken_AttemptsRefresh() throws Exception {
        // Given
        String expiredToken = "expired-token";
        String refreshToken = "refresh-token";
        String newAccessToken = "new-access-token";

        Cookie accessTokenCookie = TestCookieUtil.createTestCookie("access_token", expiredToken);
        Cookie refreshTokenCookie = TestCookieUtil.createTestCookie("refresh_token", refreshToken);
        Authentication mockAuth = mock(Authentication.class);

        when(request.getCookies()).thenReturn(new Cookie[]{accessTokenCookie, refreshTokenCookie});
        when(jwtTokenProvider.validateToken(expiredToken, false))
                .thenThrow(new InvalidTokenException("Token expired"));
        when(jwtTokenProvider.validateToken(refreshToken, true)).thenReturn(true);
        when(jwtTokenProvider.getAuthentication(refreshToken)).thenReturn(mockAuth);
        when(jwtTokenProvider.createAccessToken(mockAuth)).thenReturn(newAccessToken);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(response, times(1)).addCookie(argThat(cookie ->
                cookie.getName().equals("access_token") &&
                        cookie.getValue().equals(newAccessToken)
        ));
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
    }

    @Test
    void doFilterInternal_WithoutToken_ContinuesChain() throws Exception {
        // Given
        when(request.getCookies()).thenReturn(null);

        // When
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}
