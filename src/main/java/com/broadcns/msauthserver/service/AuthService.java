package com.broadcns.msauthserver.service;

import com.broadcns.msauthserver.entity.SignupRequest;
import com.broadcns.msauthserver.entity.User;
import com.broadcns.msauthserver.jwt.GitLabOAuth2TokenProvider;
import com.broadcns.msauthserver.jwt.JwtTokenProvider;
import com.broadcns.msauthserver.jwt.TokenResponse;
import com.broadcns.msauthserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {


    private final JwtTokenProvider jwtTokenProvider;
    private final GitLabOAuth2TokenProvider gitLabOAuth2TokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public Authentication authenticate(String email, String password) {
        // Spring Security를 통한 인증
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
    }

    public TokenResponse generateToken(Authentication authentication) {
        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);
        User principal = (User) authentication.getPrincipal();
        refreshTokenService.storeRefreshToken(principal.getEmail(), refreshToken);

        return new TokenResponse(accessToken, refreshToken);
    }

    public TokenResponse refreshAccessToken(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Invalid Refresh Token");
        }

        Map<String, String> userData = jwtTokenProvider.getUserFromToken(refreshToken);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userData.get("email"), null, null);
        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);

        return new TokenResponse(newAccessToken, refreshToken);
    }

    public void registerUser(SignupRequest signupRequest) {
        // 이메일 중복 확인
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());

        // 새로운 사용자 생성
        User user = new User();
        user.setEmail(signupRequest.getEmail());
        user.setPassword(encodedPassword); // 암호화된 비밀번호 설정
        user.setUserName(signupRequest.getUserName());
        user.setEnabled(true); // 계정 활성화

        // 사용자 저장
        userRepository.save(user);
    }
}
