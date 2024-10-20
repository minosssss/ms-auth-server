package com.broadcns.msauthserver.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/user-info")
    public String getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return "User info: " + authentication.getName(); // 인증된 유저의 이름 반환
    }

    // Protected endpoint
    @GetMapping("/user")
    public String userEndpoint(@AuthenticationPrincipal Jwt jwt, Authentication authentication) throws JsonProcessingException {
        // Authentication 객체에서 클레임 정보 추출
        Jwt principal = (Jwt) authentication.getPrincipal();

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", principal.getClaimAsString("sub"));
        claims.put("preferred_username", principal.getClaimAsString("preferred_username"));
        claims.put("roles", principal.getClaimAsStringList("roles"));
        claims.put("oid", principal.getClaimAsString("oid"));
        claims.put("iss", principal.getClaimAsString("iss"));

        return new ObjectMapper().writeValueAsString(claims);
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint";
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedApi(@AuthenticationPrincipal OAuth2User principal) {
        return ResponseEntity.ok("This is a protected API");
    }

    @GetMapping("/email")
    @ResponseBody
    public String user() {
        return "User read success.";
    }

    @GetMapping("/User.Read")
    @ResponseBody
    public String userRead(@AuthenticationPrincipal Jwt jwt, Authentication authentication) throws JsonProcessingException {
        return "User read success.";
    }
}