package com.sparta.jwt.domain.auth.dto.response;

import com.sparta.jwt.domain.user.entity.User;
import com.sparta.jwt.domain.user.enums.UserRole;
import lombok.Getter;

@Getter
public class SignupResponse {

    private final String userName;
    private final String nickName;
    private final UserRole userRole;


    public SignupResponse(User savedUser) {
        this.userName = savedUser.getUserName();
        this.nickName = savedUser.getNickName();
        this.userRole = savedUser.getUserRole();
    }

}
