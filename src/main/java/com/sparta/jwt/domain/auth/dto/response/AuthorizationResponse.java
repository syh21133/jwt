package com.sparta.jwt.domain.auth.dto.response;

import com.sparta.jwt.domain.user.entity.User;
import com.sparta.jwt.domain.user.enums.UserRole;
import lombok.Getter;

@Getter
public class AuthorizationResponse {

	private final String userName;
	private final String nickName;
	private final UserRole userRole;


	public AuthorizationResponse(User user) {
		this.userName = user.getUserName();
		this.nickName = user.getNickName();
		this.userRole = user.getUserRole();
	}

}
