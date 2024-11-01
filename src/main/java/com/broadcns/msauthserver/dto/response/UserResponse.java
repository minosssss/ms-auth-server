package com.broadcns.msauthserver.dto.response;

import com.broadcns.msauthserver.entity.Role;
import com.broadcns.msauthserver.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class UserResponse {
    private final Long id;
    private final String email;
    private final String name;
    private final Set<String> roles;

    // User 엔티티를 받는 생성자
    public UserResponse(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.name = user.getName();
        this.roles = user.getRoles().stream()
                .map(Role::name)
                .collect(Collectors.toSet());
    }

    // 테스트를 위한 모든 필드를 받는 생성자
    @Builder
    public UserResponse(Long id, String email, String name, Set<String> roles) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.roles = roles;
    }
}