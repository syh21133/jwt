package com.sparta.jwt.domain.user.entity;

import com.sparta.jwt.domain.user.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(unique = true)
	private String userName;
	private String password;
	private String nickName;
	@Enumerated(EnumType.STRING)
	private UserRole userRole;

	public User(String userName, String password, String nickName,UserRole userRole) {
		this.userName = userName;
		this.password = password;
		this.nickName = nickName;
		this.userRole = userRole;
	}

	public void updateAuthorization(UserRole userRole) {
		this.userRole = userRole;
	}
}
