package com.broadcns.msauthserver.service;

import com.broadcns.msauthserver.entity.GitlabUserInfo;
import com.broadcns.msauthserver.entity.Role;
import com.broadcns.msauthserver.entity.SignupRequest;
import com.broadcns.msauthserver.entity.User;
import com.broadcns.msauthserver.exception.InvalidTokenException;
import com.broadcns.msauthserver.jwt.GitLabOAuth2TokenProvider;
import com.broadcns.msauthserver.jwt.JwtTokenProvider;
import com.broadcns.msauthserver.jwt.TokenResponse;
import com.broadcns.msauthserver.repository.UserRepository;
import com.broadcns.msauthserver.utils.CookieUtil;
import com.broadcns.msauthserver.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.file.attribute.UserPrincipal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.access-token-validity}")
    private long accessTokenValidity;

    @Value("${jwt.refresh-token-validity}")
    private long refreshTokenValidity;

    public User getCurrentUser() {
        String email = SecurityUtil.getCurrentUserEmail();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void refreshToken(String refreshToken, HttpServletResponse response) {
        if (!jwtTokenProvider.validateToken(refreshToken, true)) {
            throw new InvalidTokenException("Invalid refresh token");
        }

        Authentication authentication = jwtTokenProvider.getAuthentication(refreshToken);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        // 새로운 토큰을 쿠키에 설정
        CookieUtil.addCookie(response, "access_token", newAccessToken, accessTokenValidity);
        CookieUtil.addCookie(response, "refresh_token", newRefreshToken, refreshTokenValidity);

        // 사용자의 refreshToken 업데이트
        User user = getCurrentUser();
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // 현재 사용자의 refreshToken 제거
        User user = getCurrentUser();
        user.setRefreshToken(null);
        userRepository.save(user);

        // 쿠키 삭제
        CookieUtil.deleteCookie(request,response,"access_token");
        CookieUtil.deleteCookie(request,response,"refresh_token");
    }
}
