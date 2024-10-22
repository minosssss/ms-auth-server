package com.broadcns.msauthserver.config;

import com.broadcns.msauthserver.jwt.GitLabOAuth2TokenProvider;
import com.broadcns.msauthserver.jwt.JwtAuthenticationFilter;
import com.broadcns.msauthserver.jwt.OpaqueTokenAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${gitlab.base-url}")
    private String gitlabBaseUrl;

    @Value("${gitlab.client-id}")
    private String gitlabClientId;

    @Value("${gitlab.client-secret}")
    private String gitlabClientSecret;

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final GitLabOAuth2TokenProvider gitLabOAuth2TokenProvider;
    private final OpaqueTokenAuthenticationFilter opaqueTokenAuthenticationFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable) // form을 통한 로그인 비활성화
                .httpBasic(AbstractHttpConfigurer::disable) // security 기본 인증 사용 비활성화
                .sessionManagement(session -> { // 세션을 사용하지 않기 때문에 세션 관리를 stateless로 설정
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
//                .exceptionHandling(exception -> // Exception part는 추후 추가
//                {
//                    exception.authenticationEntryPoint(authenticationEntryPoint); // 인증 에러
//                    exception.accessDeniedHandler(accessDeniedHandled); // 인가 에러
//                })
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PathRequest.toH2Console()).permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .headers(headers -> headers.frameOptions(FrameOptionsConfig::sameOrigin))
                .oauth2ResourceServer((resourceServer) -> resourceServer
                        .opaqueToken(token -> token
                                .introspectionUri(gitlabBaseUrl + "/oauth/introspect")
                                .introspectionClientCredentials(gitlabClientId,gitlabClientSecret)
                        ));
        http.addFilterBefore(opaqueTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


}