package com.oraclejava.demoboard.board;

import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.oraclejava.demoboard.board.dto.BoardCreateRequest;
import com.oraclejava.demoboard.board.dto.BoardDeleteRequest;
import com.oraclejava.demoboard.board.dto.BoardResponse;
import com.oraclejava.demoboard.board.dto.BoardSummaryResponse;
import com.oraclejava.demoboard.board.dto.BoardUpdateRequest;
import com.oraclejava.demoboard.board.dto.PageResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 답글형 익명 게시판 API.
 * <p>
 * 작성/답글/수정은 두 가지 형식을 받는다.
 * <ul>
 * <li>application/json : 첨부파일 없이 JSON 본문만</li>
 * <li>multipart/form-data : "board" 파트(JSON) + 선택적 "file" 파트(첨부파일)</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/boards")
@Validated
public class BoardController {

	private final BoardService service;

	private final BoardFileStorage fileStorage;

	public BoardController(BoardService service, BoardFileStorage fileStorage) {
		this.service = service;
		this.fileStorage = fileStorage;
	}

	// ---- Read ----

	@GetMapping
	public PageResponse<BoardSummaryResponse> list(@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {
		return PageResponse.of(service.list(page, size), BoardSummaryResponse::from);
	}

	@GetMapping("/{boardNum}")
	public BoardResponse read(@PathVariable long boardNum) {
		return BoardResponse.from(service.read(boardNum));
	}

	@GetMapping("/{boardNum}/file")
	public ResponseEntity<Resource> download(@PathVariable long boardNum) {
		Board board = service.get(boardNum);
		Resource resource = board.getBoardFile() == null ? null : fileStorage.load(board.getBoardFile());
		if (resource == null) {
			return ResponseEntity.notFound().build();
		}
		ContentDisposition disposition = ContentDisposition.attachment()
			.filename(BoardFileStorage.originalName(board.getBoardFile()), StandardCharsets.UTF_8)
			.build();
		return ResponseEntity.ok()
			.header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
			.contentType(MediaType.APPLICATION_OCTET_STREAM)
			.body(resource);
	}

	// ---- Create (원글) ----

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BoardResponse> create(@Valid @RequestBody BoardCreateRequest req) {
		return created(service.create(req, null));
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BoardResponse> createMultipart(@Valid @RequestPart("board") BoardCreateRequest req,
			@RequestPart(value = "file", required = false) MultipartFile file) {
		return created(service.create(req, file));
	}

	// ---- Create (답글) ----

	@PostMapping(path = "/{boardNum}/replies", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<BoardResponse> reply(@PathVariable long boardNum,
			@Valid @RequestBody BoardCreateRequest req) {
		return created(service.reply(boardNum, req, null));
	}

	@PostMapping(path = "/{boardNum}/replies", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<BoardResponse> replyMultipart(@PathVariable long boardNum,
			@Valid @RequestPart("board") BoardCreateRequest req,
			@RequestPart(value = "file", required = false) MultipartFile file) {
		return created(service.reply(boardNum, req, file));
	}

	// ---- Update ----

	@PutMapping(path = "/{boardNum}", consumes = MediaType.APPLICATION_JSON_VALUE)
	public BoardResponse update(@PathVariable long boardNum, @Valid @RequestBody BoardUpdateRequest req) {
		return BoardResponse.from(service.update(boardNum, req, null));
	}

	@PutMapping(path = "/{boardNum}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public BoardResponse updateMultipart(@PathVariable long boardNum,
			@Valid @RequestPart("board") BoardUpdateRequest req,
			@RequestPart(value = "file", required = false) MultipartFile file) {
		return BoardResponse.from(service.update(boardNum, req, file));
	}

	// ---- Delete ----

	@DeleteMapping("/{boardNum}")
	public ResponseEntity<Void> delete(@PathVariable long boardNum, @Valid @RequestBody BoardDeleteRequest req) {
		service.delete(boardNum, req.boardPass());
		return ResponseEntity.noContent().build();
	}

	private static ResponseEntity<BoardResponse> created(Board board) {
		return ResponseEntity.created(URI.create("/api/boards/" + board.getBoardNum()))
			.body(BoardResponse.from(board));
	}

}
