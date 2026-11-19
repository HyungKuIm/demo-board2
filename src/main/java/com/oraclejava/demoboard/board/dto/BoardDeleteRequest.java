package com.oraclejava.demoboard.board.dto;

import jakarta.validation.constraints.NotBlank;

/** 글 삭제 요청. */
public record BoardDeleteRequest(@NotBlank String boardPass) {
}
