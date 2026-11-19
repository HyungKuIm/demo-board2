package com.oraclejava.demoboard.board.dto;

import java.time.LocalDateTime;

import com.oraclejava.demoboard.board.Board;

/** 글 상세 응답. board_pass 는 절대 내보내지 않는다. */
public record BoardResponse(
		Long boardNum,
		String boardName,
		String boardSubject,
		String boardContent,
		String boardFile,
		long reRef,
		int reLev,
		int reStep,
		int boardReadcount,
		LocalDateTime boardDate) {

	public static BoardResponse from(Board b) {
		return new BoardResponse(b.getBoardNum(), b.getBoardName(), b.getBoardSubject(), b.getBoardContent(),
				b.getBoardFile(), b.getReRef(), b.getReLev(), b.getReStep(), b.getBoardReadcount(),
				b.getBoardDate());
	}

}
