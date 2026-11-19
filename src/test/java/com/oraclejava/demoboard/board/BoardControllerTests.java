package com.oraclejava.demoboard.board;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTests {

	@TempDir
	static Path uploadDir;

	@DynamicPropertySource
	static void props(DynamicPropertyRegistry registry) {
		registry.add("board.upload-dir", uploadDir::toString);
	}

	@Autowired
	private MockMvc mvc;

	@Autowired
	private BoardRepository repository;

	@BeforeEach
	void clean() {
		repository.deleteAll();
	}

	@Test
	void createReadUpdateDelete() throws Exception {
		long num = create("익명", "1234", "첫 글", "내용");

		// 상세 조회 → 조회수 증가, 비밀번호는 응답에 없음
		mvc.perform(get("/api/boards/{n}", num))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.board_num").value(num))
			.andExpect(jsonPath("$.board_subject").value("첫 글"))
			.andExpect(jsonPath("$.re_ref").value(num))
			.andExpect(jsonPath("$.re_lev").value(0))
			.andExpect(jsonPath("$.re_step").value(0))
			.andExpect(jsonPath("$.board_readcount").value(1))
			.andExpect(jsonPath("$.board_date").exists())
			.andExpect(jsonPath("$.board_pass").doesNotExist());

		// 잘못된 비밀번호로 수정 → 403
		mvc.perform(put("/api/boards/{n}", num).contentType(MediaType.APPLICATION_JSON)
			.content(updateJson("wrong", "익명", "수정", "수정 내용")))
			.andExpect(status().isForbidden());

		mvc.perform(put("/api/boards/{n}", num).contentType(MediaType.APPLICATION_JSON)
			.content(updateJson("1234", "익명2", "수정", "수정 내용")))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.board_name").value("익명2"))
			.andExpect(jsonPath("$.board_subject").value("수정"));

		mvc.perform(delete("/api/boards/{n}", num).contentType(MediaType.APPLICATION_JSON)
			.content("{\"board_pass\":\"wrong\"}"))
			.andExpect(status().isForbidden());

		mvc.perform(delete("/api/boards/{n}", num).contentType(MediaType.APPLICATION_JSON)
			.content("{\"board_pass\":\"1234\"}"))
			.andExpect(status().isNoContent());

		mvc.perform(get("/api/boards/{n}", num)).andExpect(status().isNotFound());
	}

	@Test
	void repliesAreOrderedAsThread() throws Exception {
		long a = create("a", "1234", "A", "원글 A");
		long b = create("b", "1234", "B", "원글 B");
		long a1 = reply(a, "A-1");
		long a2 = reply(a, "A-2"); // 같은 부모의 더 최근 답글은 위로
		long a1x = reply(a1, "A-1-x"); // 답글의 답글

		// 기대 순서: B, A, A-2, A-1, A-1-x
		mvc.perform(get("/api/boards"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.total_elements").value(5))
			.andExpect(jsonPath("$.content[0].board_num").value(b))
			.andExpect(jsonPath("$.content[1].board_num").value(a))
			.andExpect(jsonPath("$.content[2].board_num").value(a2))
			.andExpect(jsonPath("$.content[2].re_lev").value(1))
			.andExpect(jsonPath("$.content[3].board_num").value(a1))
			.andExpect(jsonPath("$.content[4].board_num").value(a1x))
			.andExpect(jsonPath("$.content[4].re_ref").value(a))
			.andExpect(jsonPath("$.content[4].re_lev").value(2))
			.andExpect(jsonPath("$.content[0].board_content").doesNotExist());
	}

	@Test
	void validationErrorReturns400() throws Exception {
		mvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON)
			.content("{\"board_name\":\"\",\"board_pass\":\"1\",\"board_subject\":\"s\",\"board_content\":\"c\"}"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void replyToMissingPostReturns404() throws Exception {
		mvc.perform(post("/api/boards/{n}/replies", 9999).contentType(MediaType.APPLICATION_JSON)
			.content(createJson("x", "1234", "s", "c")))
			.andExpect(status().isNotFound());
	}

	@Test
	void uploadAndDownloadAttachment() throws Exception {
		MockMultipartFile board = new MockMultipartFile("board", "", MediaType.APPLICATION_JSON_VALUE,
				createJson("익명", "1234", "첨부 글", "내용").getBytes(StandardCharsets.UTF_8));
		MockMultipartFile file = new MockMultipartFile("file", "hello.txt", MediaType.TEXT_PLAIN_VALUE,
				"hello".getBytes(StandardCharsets.UTF_8));

		MvcResult result = mvc.perform(multipart("/api/boards").file(board).file(file))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.board_file").value(endsWith("_hello.txt")))
			.andReturn();
		long num = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.board_num")).longValue();

		mvc.perform(get("/api/boards/{n}/file", num))
			.andExpect(status().isOk())
			.andExpect(header().string("Content-Disposition", containsString("filename=\"hello.txt\"")))
			.andExpect(content().string("hello"));
	}

	private long create(String name, String pass, String subject, String content) throws Exception {
		MvcResult result = mvc.perform(post("/api/boards").contentType(MediaType.APPLICATION_JSON)
			.content(createJson(name, pass, subject, content)))
			.andExpect(status().isCreated())
			.andReturn();
		return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.board_num")).longValue();
	}

	private long reply(long parent, String subject) throws Exception {
		MvcResult result = mvc.perform(post("/api/boards/{n}/replies", parent).contentType(MediaType.APPLICATION_JSON)
			.content(createJson("re", "1234", subject, "답글")))
			.andExpect(status().isCreated())
			.andReturn();
		return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.board_num")).longValue();
	}

	private static String createJson(String name, String pass, String subject, String content) {
		return """
				{"board_name":"%s","board_pass":"%s","board_subject":"%s","board_content":"%s"}"""
			.formatted(name, pass, subject, content);
	}

	private static String updateJson(String pass, String name, String subject, String content) {
		return """
				{"board_pass":"%s","board_name":"%s","board_subject":"%s","board_content":"%s"}"""
			.formatted(pass, name, subject, content);
	}

}
