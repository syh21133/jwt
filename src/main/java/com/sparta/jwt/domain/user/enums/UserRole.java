package com.sparta.jwt.domain.user.enums;

import static com.sparta.jwt.config.exception.enums.ExceptionCode.ACCESS_DENIED;

import com.sparta.jwt.config.exception.custom.GlobalException;
import java.util.Arrays;


public enum UserRole {
    ADMIN, USER;

    public static UserRole of(String role) {
        return Arrays.stream(UserRole.values())
                .filter(r -> r.name().equalsIgnoreCase(role))
                .findFirst()
                .orElseThrow(() -> new GlobalException(ACCESS_DENIED));
    }
}
