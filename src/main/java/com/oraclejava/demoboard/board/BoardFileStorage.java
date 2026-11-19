package com.oraclejava.demoboard.board;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/** 첨부파일을 로컬 디렉터리에 저장한다. 저장 이름은 "UUID_원본파일명". */
@Component
public class BoardFileStorage {

	private final Path root;

	public BoardFileStorage(@Value("${board.upload-dir:uploads}") String uploadDir) {
		this.root = Path.of(uploadDir).toAbsolutePath().normalize();
	}

	/** @return 저장된 파일 이름, 파일이 없으면 null */
	public String store(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			return null;
		}
		String original = StringUtils.getFilename(StringUtils.cleanPath(
				file.getOriginalFilename() == null ? "file" : file.getOriginalFilename().replace('\\', '/')));
		if (!StringUtils.hasText(original) || original.contains("..")) {
			original = "file";
		}
		String storedName = UUID.randomUUID() + "_" + original;
		Path target = resolve(storedName);
		try (InputStream in = file.getInputStream()) {
			Files.createDirectories(root);
			Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
		}
		catch (IOException ex) {
			throw new UncheckedIOException("첨부파일 저장 실패", ex);
		}
		return storedName;
	}

	/** @return 파일 리소스, 없으면 null */
	public Resource load(String storedName) {
		Path path = resolve(storedName);
		return Files.isReadable(path) ? new FileSystemResource(path) : null;
	}

	public void delete(String storedName) {
		if (storedName == null) {
			return;
		}
		try {
			Files.deleteIfExists(resolve(storedName));
		}
		catch (IOException ex) {
			// 파일 삭제 실패가 글 삭제/수정을 막지는 않는다
		}
	}

	/** "UUID_원본파일명" 에서 원본 파일명만 꺼낸다. */
	public static String originalName(String storedName) {
		int idx = storedName.indexOf('_');
		return idx >= 0 ? storedName.substring(idx + 1) : storedName;
	}

	private Path resolve(String storedName) {
		Path path = root.resolve(storedName).normalize();
		if (!path.startsWith(root)) {
			throw new IllegalArgumentException("잘못된 파일 경로");
		}
		return path;
	}

}
