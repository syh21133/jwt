package com.sparta.jwt.domain.auth.service;

import static com.sparta.jwt.config.exception.enums.ExceptionCode.*;

import com.sparta.jwt.config.JwtUtil;
import com.sparta.jwt.domain.auth.dto.request.SigninRequest;
import com.sparta.jwt.domain.auth.dto.request.SignupRequest;
import com.sparta.jwt.domain.auth.dto.response.AuthorizationResponse;
import com.sparta.jwt.domain.auth.dto.response.SigninResponse;
import com.sparta.jwt.domain.auth.dto.response.SignupResponse;
import com.sparta.jwt.config.exception.custom.GlobalException;
import com.sparta.jwt.config.security.UserDetailsImpl;
import com.sparta.jwt.domain.user.entity.User;
import com.sparta.jwt.domain.user.enums.UserRole;
import com.sparta.jwt.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;


	@Transactional
	public SignupResponse signup(SignupRequest signupRequest) {

		if (userRepository.existsByUserName(signupRequest.getUserName())) {
			throw new GlobalException(USER_ALREADY_EXISTS);
		}
		String encodedPassword = passwordEncoder.encode(signupRequest.getPassword());
		UserRole userRole = UserRole.of(signupRequest.getUserRole());

		User newUser = new User(
			signupRequest.getUserName(),
			encodedPassword,
			signupRequest.getNickName(),
			userRole
		);
		User savedUser = userRepository.save(newUser);

		return new SignupResponse(savedUser);
	}

	public SigninResponse signin(SigninRequest signinRequest){
		User user = userRepository.findByUserName(signinRequest.getUserName()).orElseThrow(
			() -> new GlobalException(INVALID_CREDENTIALS));

		if(!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())){
			throw new GlobalException(INVALID_CREDENTIALS);
		}

		String bearerToken = jwtUtil.createToken(user.getId(), user.getUserName(),
			user.getNickName(),user.getUserRole());


		return new SigninResponse(bearerToken);
	}

	@Transactional
	public AuthorizationResponse authorization(Long userId, UserDetailsImpl authMember) {

		if(!authMember.getUserRole().equals("ADMIN")){
			throw new GlobalException(ACCESS_DENIED);
		}
		User user = userRepository.findById(userId).orElseThrow(() -> new GlobalException(USER_ALREADY_EXISTS));
		UserRole userRole = UserRole.of("admin");

		user.updateAuthorization(userRole);

		return new AuthorizationResponse(user);

	}
}
