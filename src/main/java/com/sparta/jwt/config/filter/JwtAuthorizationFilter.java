package com.sparta.jwt.config.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.jwt.config.JwtUtil;
import com.sparta.jwt.config.security.UserDetailsServiceImpl;
import com.sparta.jwt.config.exception.dto.ResponseExceptionCode;
import com.sparta.jwt.config.exception.enums.ExceptionCode;
import java.io.IOException;

import java.util.Map;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;


import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j(topic = "JWT 검증 및 인가")
public class JwtAuthorizationFilter extends OncePerRequestFilter {

	private final JwtUtil jwtUtil;
	private final UserDetailsServiceImpl userDetailsService;

	public JwtAuthorizationFilter(JwtUtil jwtUtil, UserDetailsServiceImpl userDetailsService) {
		this.jwtUtil = jwtUtil;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain) throws
		ServletException,
		IOException {

		String tokenValue = jwtUtil.getTokenFromRequest(req);

		if (StringUtils.hasText(tokenValue)) {
			// JWT 토큰 substring
			tokenValue = jwtUtil.substringToken(tokenValue);
			log.info(tokenValue);

			if (!jwtUtil.validateToken(tokenValue)) {
				log.error("Token Error");
				writeErrorResponse(res, ExceptionCode.INVALID_TOKEN);
				return;
			}

			Claims info = jwtUtil.extractClaims(tokenValue);
			String userName = info.get("userName", String.class);

			try {
				setAuthentication(userName);
			} catch (Exception e) {
				log.error(e.getMessage());
				return;
			}
		}

		filterChain.doFilter(req, res);
	}

	// 인증 처리
	public void setAuthentication(String username) {
		SecurityContext context = SecurityContextHolder.createEmptyContext();
		Authentication authentication = createAuthentication(username);
		context.setAuthentication(authentication);

		SecurityContextHolder.setContext(context);
	}

	// 인증 객체 생성
	private Authentication createAuthentication(String userName) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
		return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
	}
	private void writeErrorResponse(HttpServletResponse response, ExceptionCode exceptionCode) throws IOException {
		response.setStatus(exceptionCode.getHttpStatus().value());
		response.setContentType("application/json;charset=UTF-8");

		ResponseExceptionCode responseExceptionCode = ResponseExceptionCode.builder()
			.code(exceptionCode.name())
			.message(exceptionCode.getMessage())
			.build();

		ObjectMapper objectMapper = new ObjectMapper();
		String jsonResponse = objectMapper.writeValueAsString(Map.of("error", responseExceptionCode));

		response.getWriter().write(jsonResponse);
	}
}
