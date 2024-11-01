package com.broadcns.msauthserver.data;

import com.broadcns.msauthserver.entity.Role;
import com.broadcns.msauthserver.entity.User;

import java.util.Set;

public class UserData {

    public static User createUser() {
        User user = new User();
        user.setEmail("admin@example.com");
        user.setName("admin");
        user.setGitlabId("123");
        user.setRoles(Set.of(Role.ROLE_ADMIN));
        return user;
    }
}
