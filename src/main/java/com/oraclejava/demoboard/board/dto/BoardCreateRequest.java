package com.oraclejava.demoboard.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 원글/답글 작성 요청. */
public record BoardCreateRequest(
		@NotBlank @Size(max = 20) String boardName,
		@NotBlank @Size(min = 4, max = 30) String boardPass,
		@NotBlank @Size(max = 100) String boardSubject,
		@NotBlank String boardContent) {
}
