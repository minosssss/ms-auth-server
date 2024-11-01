package com.broadcns.msauthserver.jwt;

import com.broadcns.msauthserver.dto.JwtProperties;
import com.broadcns.msauthserver.entity.User;
import com.broadcns.msauthserver.exception.AuthenticationException;
import com.broadcns.msauthserver.exception.InvalidTokenException;
import com.broadcns.msauthserver.service.UserInfoDetailService;
import com.sun.security.auth.UserPrincipal;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.validation.Payload;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY;


    private final JwtProperties jwtProperties;
    private final UserInfoDetailService userDetailsService;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(this.SECRET_KEY));
    }


    public String createAccessToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.getAccessTokenValidity());
    }

    public String createRefreshToken(Authentication authentication) {
        return createToken(authentication, jwtProperties.getRefreshTokenValidity());
    }

    private String createToken(Authentication authentication, long accessExpiredTime) {
        String username;
        if (authentication.getPrincipal() instanceof UserDetails) {
            username = ((UserDetails) authentication.getPrincipal()).getUsername();
        } else {
            username = authentication.getName();
        }
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Instant now = Instant.now();
        Instant validity = now.plus(accessExpiredTime, ChronoUnit.SECONDS);
        return Jwts.builder()
                .subject(username)
                .claim("roles", authorities.stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList()))
                .signWith(getSigningKey(), Jwts.SIG.HS256)
                .issuedAt(Date.from(now))
                .expiration(Date.from(validity))
                .compact();
    }

    private List<String> getAuthorities(UserDetails userDetails) {
        return userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get("roles").toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());


        return new UsernamePasswordAuthenticationToken(claims.getSubject(), token, authorities);
    }

    public boolean validateToken(String token, boolean isRefreshToken) {
        try {
            parseClaims(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature");
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token");
            throw new AuthenticationException("Token has expired");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token");
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty");
        }
        return false;
    }

    public Claims parseClaims(String token) {
        Claims payload = Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (payload.getExpiration().before(new Date())) {
            throw new InvalidTokenException("Token has expired");
        }
        return payload;
    }

    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean isTokenExpiringSoon(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            // 만료 10분 전
            return expiration.getTime() - System.currentTimeMillis() < 600000;
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

}
