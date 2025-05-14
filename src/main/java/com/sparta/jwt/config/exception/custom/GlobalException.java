package com.sparta.jwt.config.exception.custom;

import com.sparta.jwt.config.exception.enums.ExceptionCode;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException {
	private final ExceptionCode exceptionCode;

	public GlobalException(ExceptionCode exceptionCode) {
		super(exceptionCode.getMessage());
		this.exceptionCode = exceptionCode;
	}
}
