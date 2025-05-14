package com.sparta.jwt.config.filter;


import static com.sparta.jwt.config.exception.enums.ExceptionCode.INVALID_CREDENTIALS;

import com.sparta.jwt.config.JwtUtil;
import com.sparta.jwt.domain.auth.dto.request.SigninRequest;
import com.sparta.jwt.config.exception.custom.GlobalException;
import com.sparta.jwt.config.security.UserDetailsImpl;
import com.sparta.jwt.domain.user.entity.User;
import com.sparta.jwt.domain.user.enums.UserRole;
import com.sparta.jwt.domain.user.repository.UserRepository;
import java.io.IOException;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "로그인 및 JWT 생성")
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

	private final JwtUtil jwtUtil;
	private final UserRepository userRepository;


	public JwtAuthenticationFilter(JwtUtil jwtUtil, UserRepository userRepository) {
		this.jwtUtil = jwtUtil;
		this.userRepository = userRepository;
	}

	@Override
	public Authentication attemptAuthentication(HttpServletRequest request,
		HttpServletResponse response) throws
		AuthenticationException {
		log.info("로그인 시도");
		try {
			SigninRequest requestDto = new ObjectMapper().readValue(request.getInputStream(),
				SigninRequest.class);

			User user = userRepository.findByUserName(requestDto.getUserName())
				.orElseThrow(() -> new GlobalException(INVALID_CREDENTIALS));


			UsernamePasswordAuthenticationToken authToken =
				new UsernamePasswordAuthenticationToken(
					requestDto.getUserName(),
					requestDto.getPassword(),
					null
				);


			return getAuthenticationManager().authenticate(authToken);

		} catch (IOException e) {
			log.error(e.getMessage());
			throw new RuntimeException(e.getMessage());
		}
	}

	@Override
	protected void successfulAuthentication(HttpServletRequest request,
		HttpServletResponse response, FilterChain chain, Authentication authResult)
		throws IOException,
		ServletException {
		log.info("로그인 성공 및 JWT 생성");
		UserRole userRole = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getUserRole();

		String token = jwtUtil.createToken(
			((UserDetailsImpl) authResult.getPrincipal()).getUser().getId(),
			((UserDetailsImpl) authResult.getPrincipal()).getUser().getUserName(),
			((UserDetailsImpl) authResult.getPrincipal()).getUser().getNickName(),
			userRole);
		jwtUtil.addJwtToCookie(token, response);

	}

	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request,
		HttpServletResponse response, AuthenticationException failed)
		throws IOException, ServletException {
		log.info("로그인 실패");
		response.setStatus(401);
	}
}
