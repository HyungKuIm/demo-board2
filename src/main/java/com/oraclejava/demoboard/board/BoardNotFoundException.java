package com.oraclejava.demoboard.board;

public class BoardNotFoundException extends RuntimeException {

	public BoardNotFoundException(long boardNum) {
		super("게시글을 찾을 수 없습니다: " + boardNum);
	}

}
