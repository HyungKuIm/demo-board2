-- 답글형 익명 게시판 MySQL 스키마
-- (ddl-auto=update 로 자동 생성되지만, 수동 생성/운영 배포 시 이 스크립트를 사용)

CREATE DATABASE IF NOT EXISTS board DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE board;

CREATE TABLE IF NOT EXISTS board (
    board_num       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '글 번호',
    board_name      VARCHAR(20)  NOT NULL COMMENT '글 작성자',
    board_pass      VARCHAR(100) NOT NULL COMMENT '글 비밀번호 (BCrypt 해시)',
    board_subject   VARCHAR(100) NOT NULL COMMENT '글 제목',
    board_content   LONGTEXT     NOT NULL COMMENT '글 내용',
    board_file      VARCHAR(255)          COMMENT '첨부파일 (UUID_원본파일명)',
    re_ref          BIGINT       NOT NULL COMMENT '관련 글번호',
    re_lev          INT          NOT NULL COMMENT '답글 레벨',
    re_step         INT          NOT NULL COMMENT '관련글 출력순서',
    board_readcount INT          NOT NULL DEFAULT 0 COMMENT '조회수',
    board_date      DATETIME(6)  NOT NULL COMMENT '작성일',
    PRIMARY KEY (board_num),
    INDEX idx_board_ref_step (re_ref DESC, re_step ASC)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;
