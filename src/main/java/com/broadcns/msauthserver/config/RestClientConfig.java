package com.broadcns.msauthserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.net.http.HttpClient;
import java.util.Collections;

@Configuration
public class RestClientConfig {


    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .requestFactory(new JdkClientHttpRequestFactory())  // JDK HttpClient 사용
                .baseUrl("https://gitlab.com")  // GitLab base URL
                .defaultHeaders(headers -> {
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
                })
                .build();
    }
}