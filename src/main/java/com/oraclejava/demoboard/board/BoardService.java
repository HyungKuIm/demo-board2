package com.oraclejava.demoboard.board;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.oraclejava.demoboard.board.dto.BoardCreateRequest;
import com.oraclejava.demoboard.board.dto.BoardUpdateRequest;

@Service
@Transactional(readOnly = true)
public class BoardService {

	/** 최신 묶음이 위로, 묶음 안에서는 re_step 순서대로. */
	private static final Sort LIST_ORDER = Sort.by(Sort.Order.desc("reRef"), Sort.Order.asc("reStep"));

	private final BoardRepository repository;

	private final BoardFileStorage fileStorage;

	private final PasswordEncoder passwordEncoder;

	public BoardService(BoardRepository repository, BoardFileStorage fileStorage, PasswordEncoder passwordEncoder) {
		this.repository = repository;
		this.fileStorage = fileStorage;
		this.passwordEncoder = passwordEncoder;
	}

	public Page<Board> list(int page, int size) {
		return repository.findAll(PageRequest.of(page, size, LIST_ORDER));
	}

	/** 상세 조회. 조회수를 1 증가시킨다. */
	@Transactional
	public Board read(long boardNum) {
		if (repository.increaseReadcount(boardNum) == 0) {
			throw new BoardNotFoundException(boardNum);
		}
		return get(boardNum);
	}

	/** 조회수 증가 없이 가져온다. */
	public Board get(long boardNum) {
		return repository.findById(boardNum).orElseThrow(() -> new BoardNotFoundException(boardNum));
	}

	@Transactional
	public Board create(BoardCreateRequest req, MultipartFile file) {
		String storedFile = fileStorage.store(file);
		Board board = Board.newPost(req.boardName(), passwordEncoder.encode(req.boardPass()), req.boardSubject(),
				req.boardContent(), storedFile);
		repository.saveAndFlush(board);
		board.assignOwnRef();
		return board;
	}

	@Transactional
	public Board reply(long parentNum, BoardCreateRequest req, MultipartFile file) {
		Board parent = get(parentNum);
		long reRef = parent.getReRef();
		int reStep = parent.getReStep();
		repository.shiftReplySteps(reRef, reStep);

		String storedFile = fileStorage.store(file);
		Board reply = Board.newReply(parent, req.boardName(), passwordEncoder.encode(req.boardPass()),
				req.boardSubject(), req.boardContent(), storedFile);
		return repository.save(reply);
	}

	/** file 이 주어지면 기존 첨부파일을 교체하고, 없으면 기존 첨부파일을 유지한다. */
	@Transactional
	public Board update(long boardNum, BoardUpdateRequest req, MultipartFile file) {
		Board board = get(boardNum);
		checkPassword(board, req.boardPass());
		board.edit(req.boardName(), req.boardSubject(), req.boardContent());

		String newFile = fileStorage.store(file);
		if (newFile != null) {
			String oldFile = board.getBoardFile();
			board.replaceFile(newFile);
			fileStorage.delete(oldFile);
		}
		return board;
	}

	/** 해당 글만 삭제한다. 답글은 남으며 같은 re_ref 묶음으로 계속 정렬된다. */
	@Transactional
	public void delete(long boardNum, String rawPass) {
		Board board = get(boardNum);
		checkPassword(board, rawPass);
		repository.delete(board);
		fileStorage.delete(board.getBoardFile());
	}

	private void checkPassword(Board board, String rawPass) {
		if (rawPass == null || !passwordEncoder.matches(rawPass, board.getBoardPass())) {
			throw new InvalidPasswordException();
		}
	}

}
