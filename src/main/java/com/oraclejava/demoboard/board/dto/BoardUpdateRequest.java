package com.oraclejava.demoboard.board.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 글 수정 요청. board_pass 는 작성 시 비밀번호와 일치해야 한다. */
public record BoardUpdateRequest(
		@NotBlank String boardPass,
		@NotBlank @Size(max = 20) String boardName,
		@NotBlank @Size(max = 100) String boardSubject,
		@NotBlank String boardContent) {
}
