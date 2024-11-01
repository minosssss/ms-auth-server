package com.broadcns.msauthserver.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

//    @Test
//    void testPublicEndpoints() throws Exception {
//        mockMvc.perform(get("/api/auth/gitlab/login"))
//                .andExpect(status().isOk());
//    }
//
    @Test
    void testProtectedEndpoints_WithoutAuth() throws Exception {
        mockMvc.perform(get("/api/users/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testCorsConfiguration() throws Exception {
        mockMvc.perform(options("/api/auth/gitlab/login")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().exists("Access-Control-Allow-Origin"))
                .andExpect(header().exists("Access-Control-Allow-Methods"));
    }
}
