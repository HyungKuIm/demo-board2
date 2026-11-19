package com.oraclejava.demoboard.board;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

/**
 * 답글형 게시판 글.
 * <ul>
 * <li>re_ref : 같은 글 묶음(원글 + 모든 답글)이 공유하는 원글 번호</li>
 * <li>re_lev : 답글 깊이 (원글 0, 답글 1, 답글의 답글 2 ...)</li>
 * <li>re_step : 같은 re_ref 묶음 안에서의 출력 순서 (원글 0)</li>
 * </ul>
 */
@Entity
@Table(name = "board", indexes = @Index(name = "idx_board_ref_step", columnList = "re_ref DESC, re_step ASC"))
public class Board {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long boardNum;

	@Column(nullable = false, length = 20)
	private String boardName;

	/** BCrypt 해시로 저장된다. */
	@Column(nullable = false, length = 100)
	private String boardPass;

	@Column(nullable = false, length = 100)
	private String boardSubject;

	@Lob
	@Column(nullable = false)
	private String boardContent;

	/** 서버에 저장된 첨부파일 이름 (UUID_원본파일명). 없으면 null. */
	@Column(length = 255)
	private String boardFile;

	@Column(nullable = false)
	private long reRef;

	@Column(nullable = false)
	private int reLev;

	@Column(nullable = false)
	private int reStep;

	@Column(nullable = false)
	private int boardReadcount;

	@Column(nullable = false, updatable = false)
	private LocalDateTime boardDate;

	protected Board() {
	}

	private Board(String boardName, String boardPass, String boardSubject, String boardContent, String boardFile,
			long reRef, int reLev, int reStep) {
		this.boardName = boardName;
		this.boardPass = boardPass;
		this.boardSubject = boardSubject;
		this.boardContent = boardContent;
		this.boardFile = boardFile;
		this.reRef = reRef;
		this.reLev = reLev;
		this.reStep = reStep;
		this.boardReadcount = 0;
		this.boardDate = LocalDateTime.now();
	}

	/** 원글 생성. re_ref 는 저장 후 자기 글번호로 채운다({@link #assignOwnRef()}). */
	static Board newPost(String name, String encodedPass, String subject, String content, String file) {
		return new Board(name, encodedPass, subject, content, file, 0L, 0, 0);
	}

	/** parent 바로 아래에 들어갈 답글 생성. */
	static Board newReply(Board parent, String name, String encodedPass, String subject, String content,
			String file) {
		return new Board(name, encodedPass, subject, content, file, parent.reRef, parent.reLev + 1,
				parent.reStep + 1);
	}

	void assignOwnRef() {
		this.reRef = this.boardNum;
	}

	void edit(String name, String subject, String content) {
		this.boardName = name;
		this.boardSubject = subject;
		this.boardContent = content;
	}

	void replaceFile(String file) {
		this.boardFile = file;
	}

	public Long getBoardNum() {
		return boardNum;
	}

	public String getBoardName() {
		return boardName;
	}

	public String getBoardPass() {
		return boardPass;
	}

	public String getBoardSubject() {
		return boardSubject;
	}

	public String getBoardContent() {
		return boardContent;
	}

	public String getBoardFile() {
		return boardFile;
	}

	public long getReRef() {
		return reRef;
	}

	public int getReLev() {
		return reLev;
	}

	public int getReStep() {
		return reStep;
	}

	public int getBoardReadcount() {
		return boardReadcount;
	}

	public LocalDateTime getBoardDate() {
		return boardDate;
	}

}
