package com.broadcns.msauthserver.jwt;

import jakarta.servlet.ServletException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class OpaqueTokenAuthenticationFilter extends OncePerRequestFilter {


    private final OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider;

    public OpaqueTokenAuthenticationFilter(OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider) {
        this.opaqueTokenAuthenticationProvider = opaqueTokenAuthenticationProvider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws IOException {
        String authHeader = request.getHeader("Authorization");


        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // 토큰이 Opaque Token인지 확인 (JWT가 아닌지 확인)
                if (!token.contains(".")) {  // JWT는 보통 '.'으로 구분됨
                    BearerTokenAuthenticationToken authenticationToken = new BearerTokenAuthenticationToken(token);

                    // Opaque Token을 GitLab에서 검증
                    SecurityContextHolder.getContext().setAuthentication(
                            opaqueTokenAuthenticationProvider.authenticate(authenticationToken)
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
