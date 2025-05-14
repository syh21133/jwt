package com.sparta.jwt.config.exception.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseExceptionCode {
	private String code;

	private String message;
}
