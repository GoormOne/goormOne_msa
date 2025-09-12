package com.example.storeservice.repository;

import com.example.storeservice.entity.ReviewQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ReviewQueryRepository extends JpaRepository<ReviewQuery, UUID> {
    // JPQL 업데이트 방식
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update ReviewQuery rq set rq.answerText = :answer where rq.questionId = :qid")
    int updateAnswer(@Param("qid") UUID questionId, @Param("answer") String answer);
}
