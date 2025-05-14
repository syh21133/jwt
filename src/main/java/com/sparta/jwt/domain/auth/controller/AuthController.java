package com.sparta.jwt.domain.auth.controller;

import com.sparta.jwt.domain.auth.dto.request.SigninRequest;
import com.sparta.jwt.domain.auth.dto.request.SignupRequest;
import com.sparta.jwt.domain.auth.dto.response.AuthorizationResponse;
import com.sparta.jwt.domain.auth.dto.response.SigninResponse;
import com.sparta.jwt.domain.auth.dto.response.SignupResponse;
import com.sparta.jwt.domain.auth.service.AuthService;
import com.sparta.jwt.config.security.UserDetailsImpl;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequiredArgsConstructor
public class AuthController {
	private final AuthService authService;

	@PostMapping("/auth/signup")
	public SignupResponse signup(@Valid @RequestBody SignupRequest signupRequest) {
		return ResponseEntity.ok(authService.signup(signupRequest)).getBody();
	}

	@PostMapping("/auth/signin")
	public SigninResponse signin(@Valid @RequestBody SigninRequest signinRequest) {
		return authService.signin(signinRequest);
	}

	@PatchMapping("/admin/users/{userId}/roles")
	public AuthorizationResponse authorization(@PathVariable Long userId,@AuthenticationPrincipal UserDetailsImpl authMember){
		return authService.authorization(userId,authMember);
	}
}
