package com.broadcns.msauthserver.controller;

import com.broadcns.msauthserver.dto.response.UserResponse;
import com.broadcns.msauthserver.exception.InvalidTokenException;
import com.broadcns.msauthserver.service.AuthService;
import com.broadcns.msauthserver.utils.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        return ResponseEntity.ok(new UserResponse(authService.getCurrentUser()));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<Void> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtil.getCookie(request, "refresh_token")
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));
        authService.refreshToken(refreshToken, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }


}
