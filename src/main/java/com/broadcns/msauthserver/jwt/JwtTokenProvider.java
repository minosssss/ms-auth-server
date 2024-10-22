package com.broadcns.msauthserver.jwt;

import com.broadcns.msauthserver.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Value("${jwt.expiration.expirationMs}")
    private long ACCESS_TOKEN_EXPIRATION_MILLIS;

    @Value("${jwt.expiration.refreshExpirationDays}")
    private long REFRESH_TOKEN_EXPIRATION_DAY;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String createAccessToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ZonedDateTime now = ZonedDateTime.now();
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(now.plus(ACCESS_TOKEN_EXPIRATION_MILLIS, ChronoUnit.MILLIS).toInstant()))
                .signWith(getSigningKey())
                .compact();
    }

    public String createRefreshToken(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        ZonedDateTime now = ZonedDateTime.now();
        Claims claims = Jwts
                .claims()
                .add("data", user)
                .build();

        String refreshToken = Jwts.builder()
                .claims(claims)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(now.plusDays(REFRESH_TOKEN_EXPIRATION_DAY).toInstant()))
                .signWith(getKey())
                .compact();

        // DB 저장

        return refreshToken;
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY));
    }

    private Claims getClaim(String token) {
        return Jwts
                .parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token) {
        try {
            getClaim(token);
            return true;
//        } catch (ExpiredJwtException e) {
//            throw new ExpiredAccessTokenException();
        } catch (JwtException e) {
            return false;
        }
    }

    public Map<String, String> getUserFromToken(String token) {
        Claims claim = getClaim(token);
        Map<String, String> data = (Map<String, String>) claim.get("data");
        return data;
    }

    public String getUserEmailFromToken(String token) {
        Claims claim = getClaim(token);
        // claim.getSubject()를 통해 JWT에서 subject 값을 가져옴
        return claim.getSubject();
    }

}
