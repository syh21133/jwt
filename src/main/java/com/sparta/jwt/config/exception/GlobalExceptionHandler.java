package com.sparta.jwt.config.exception;


import com.sparta.jwt.config.exception.custom.GlobalException;
import com.sparta.jwt.config.exception.dto.NotValidRequestParameter;
import com.sparta.jwt.config.exception.dto.NotValidRequestParameter.NotValidParameter;
import com.sparta.jwt.config.exception.dto.ResponseExceptionCode;
import com.sparta.jwt.config.exception.enums.ExceptionCode;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.access.AccessDeniedException;

@Slf4j(topic = "ControllerException")
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(GlobalException.class)
	public ResponseEntity<Object> handleGlobalException(GlobalException e) {
		ExceptionCode exceptionCode = e.getExceptionCode();
		log.error("{}: {}", exceptionCode, exceptionCode.getMessage());
		return ResponseEntity.status(exceptionCode.getHttpStatus())
			.body(makeResponseExceptionCode(exceptionCode));
	}



	private ResponseExceptionCode makeResponseExceptionCode(ExceptionCode exceptionCode) {
		return ResponseExceptionCode.builder()
			.code(exceptionCode.name())
			.message(exceptionCode.getMessage())
			.build();
	}

	private NotValidRequestParameter makeNotValidRequestParameter(BindException e,
		ExceptionCode exceptionCode) {
		List<NotValidParameter> notValidParameters = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(NotValidParameter::of)
			.toList();

		return NotValidRequestParameter.builder()
			.code(exceptionCode.name())
			.message(exceptionCode.getMessage())
			.notValidParameters(notValidParameters)
			.build();
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException ex) {
		// 유효성 검증 실패한 FieldError 목록
		List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

		// 요청 객체의 필드 순서 가져오기
		Class<?> targetClass = ex.getBindingResult().getTarget().getClass();
		List<String> fieldOrder = List.of(targetClass.getDeclaredFields())
			.stream()
			.map(Field::getName)
			.collect(Collectors.toList());

		// 필드 순서대로 FieldError 정렬
		Map<String, String> errors = fieldErrors.stream()
			.sorted((e1, e2) -> Integer.compare(fieldOrder.indexOf(e1.getField()), fieldOrder.indexOf(e2.getField())))
			.collect(Collectors.toMap(
				FieldError::getField,
				FieldError::getDefaultMessage,
				(existing, replacement) -> existing, // 중복 필드는 무시
				LinkedHashMap::new // 순서를 유지하는 LinkedHashMap 사용
			));

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
	}
	@ExceptionHandler({JwtException.class, IllegalArgumentException.class, ExpiredJwtException.class})
	public ResponseEntity<Object> handleJwtException(Exception e) {
		log.error("JWT 예외 발생: {}", e.getMessage());
		return ResponseEntity
			.status(ExceptionCode.INVALID_TOKEN.getHttpStatus())
			.body(makeResponseExceptionCode(ExceptionCode.INVALID_TOKEN));
	}
	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException e) {
		log.error("접근 권한 부족: {}", e.getMessage());
		return ResponseEntity
			.status(ExceptionCode.ACCESS_DENIED.getHttpStatus())
			.body(makeResponseExceptionCode(ExceptionCode.ACCESS_DENIED));
	}
}