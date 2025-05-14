package com.sparta.jwt.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sparta.jwt.domain.auth.dto.request.SigninRequest;
import com.sparta.jwt.domain.auth.dto.request.SignupRequest;
import com.sparta.jwt.domain.user.enums.UserRole;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Transactional
@Rollback
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	@DisplayName("회원가입 - 정상 케이스")
	void signup_success() throws Exception {
		SignupRequest request = new SignupRequest(
			"newuser", "password123", "닉네임", "USER"
		);

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userName").value("newuser"))
			.andExpect(jsonPath("$.nickName").value("닉네임"))
			.andExpect(jsonPath("$.userRole").value("USER"));
	}
	@Test
	@DisplayName("회원가입 - 이미 존재하는 사용자")
	void signup_userAlreadyExists() throws Exception {
		// 먼저 사용자 생성
		SignupRequest request = new SignupRequest(
			"duplicateUser", "password123", "닉네임", "USER"
		);

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		// 같은 사용자로 다시 가입 시도
		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())  // GlobalException의 응답 코드에 따라 조정
			.andExpect(jsonPath("$.message").value("이미 가입된 사용자입니다.")); // 메시지는 실제 설정에 따라 조정
	}
	@Test
	@DisplayName("로그인 - 정상 케이스")
	void signin_success() throws Exception {
		SignupRequest signupRequest = new SignupRequest("loginUser", "pass123", "닉", "USER");

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
			.andExpect(status().isOk());

		SigninRequest signinRequest = new SigninRequest("loginUser", "pass123");

		mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signinRequest)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.bearerToken").exists());
	}
	@Test
	@DisplayName("로그인 - 잘못된 비밀번호")
	void signin_invalidPassword() throws Exception {
		SignupRequest signupRequest = new SignupRequest("wrongpass", "correct123", "닉", "USER");

		mockMvc.perform(post("/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signupRequest)))
			.andExpect(status().isOk());

		SigninRequest signinRequest = new SigninRequest("wrongpass", "wrongpass");

		mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(signinRequest)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.message").value("아이디 또는 비밀번호가 올바르지 않습니다.")); // 메시지 조정
	}
	@Test
	@DisplayName("권한 부여 - 관리자")
	void authorization_byAdmin_success() throws Exception {
		// 1. 일반 유저와 관리자 회원가입
		SignupRequest user = new SignupRequest("targetUser", "pw", "유저", "USER");
		SignupRequest admin = new SignupRequest("adminUser", "adminpw", "관리자", "ADMIN");

		mockMvc.perform(post("/auth/signup")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(user)));

		mockMvc.perform(post("/auth/signup")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(admin)));

		// 2. 관리자 로그인 → 토큰 획득
		SigninRequest adminSignin = new SigninRequest("adminUser", "adminpw");

		String tokenResponse = mockMvc.perform(post("/auth/signin")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(adminSignin)))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();


		String bearerToken = objectMapper.readTree(tokenResponse).get("bearerToken").asText();
		String jwt = bearerToken.replace("Bearer ", "").trim();

		// 3. 권한 부여 요청
		// userId는 실제 DB에서 ID를 가져와야 하지만 간단히 하기 위해 1이라고 가정
		mockMvc.perform(patch("/admin/users/1/roles")
				.header("Authorization", "Bearer " + jwt)) // ✅ 올바른 형태로 헤더 전달
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.userRole").value("ADMIN"));
	}


}
