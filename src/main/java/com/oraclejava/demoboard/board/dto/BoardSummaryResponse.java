package com.oraclejava.demoboard.board.dto;

import java.time.LocalDateTime;

import com.oraclejava.demoboard.board.Board;

/** 목록용 응답 (본문 제외). re_lev 로 들여쓰기하면 답글 트리가 된다. */
public record BoardSummaryResponse(
		Long boardNum,
		String boardName,
		String boardSubject,
		String boardFile,
		long reRef,
		int reLev,
		int reStep,
		int boardReadcount,
		LocalDateTime boardDate) {

	public static BoardSummaryResponse from(Board b) {
		return new BoardSummaryResponse(b.getBoardNum(), b.getBoardName(), b.getBoardSubject(), b.getBoardFile(),
				b.getReRef(), b.getReLev(), b.getReStep(), b.getBoardReadcount(), b.getBoardDate());
	}

}
