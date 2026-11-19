package com.oraclejava.demoboard.board;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BoardRepository extends JpaRepository<Board, Long> {

	/** 답글이 끼어들 자리를 만들기 위해, 같은 묶음에서 부모보다 뒤에 있는 글들을 한 칸씩 민다. */
	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update Board b set b.reStep = b.reStep + 1 where b.reRef = :reRef and b.reStep > :reStep")
	int shiftReplySteps(@Param("reRef") long reRef, @Param("reStep") int reStep);

	@Modifying(flushAutomatically = true, clearAutomatically = true)
	@Query("update Board b set b.boardReadcount = b.boardReadcount + 1 where b.boardNum = :boardNum")
	int increaseReadcount(@Param("boardNum") long boardNum);

}
