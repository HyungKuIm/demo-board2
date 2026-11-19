package com.oraclejava.demoboard.board;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/** 오류를 RFC 9457 Problem Detail(JSON)로 응답한다. 검증 오류(400) 등은 상위 클래스가 처리. */
@RestControllerAdvice
public class BoardExceptionHandler extends ResponseEntityExceptionHandler {

	@ExceptionHandler(BoardNotFoundException.class)
	ProblemDetail handleNotFound(BoardNotFoundException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
	}

	@ExceptionHandler(InvalidPasswordException.class)
	ProblemDetail handleInvalidPassword(InvalidPasswordException ex) {
		return ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
	}

}
