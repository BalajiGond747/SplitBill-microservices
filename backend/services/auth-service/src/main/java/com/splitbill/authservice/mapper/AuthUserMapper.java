package com.splitbill.authservice.mapper;

import com.splitbill.authservice.entity.AuthUser;
import org.springframework.stereotype.Component;

@Component
public class AuthUserMapper {

    public AuthUser toEntity(Long userId, String encodedPassword) {

        return AuthUser.builder()
                .userId(userId)
                .password(encodedPassword)
                .role(AuthUser.Role.USER)
                .provider(AuthUser.Provider.LOCAL)
                .enabled(true)
                .build();
    }
}