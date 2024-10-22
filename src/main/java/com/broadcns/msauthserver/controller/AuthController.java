package com.broadcns.msauthserver.controller;

import com.broadcns.msauthserver.entity.LoginRequest;
import com.broadcns.msauthserver.entity.SignupRequest;
import com.broadcns.msauthserver.jwt.TokenResponse;
import com.broadcns.msauthserver.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authService.authenticate(loginRequest.getEmail(), loginRequest.getPassword());
        TokenResponse tokenResponse = authService.generateToken(authentication);
        return ResponseEntity.ok(tokenResponse);
    }

    // 회원가입 엔드포인트
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequest signupRequest) {
        try {
            // 사용자 등록 로직 호출
            authService.registerUser(signupRequest);
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
