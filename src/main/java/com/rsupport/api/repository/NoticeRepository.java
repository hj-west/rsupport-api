package com.rsupport.api.repository;

import com.rsupport.api.entity.Notice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface NoticeRepository extends JpaRepository<Notice, Long> {

    @Query("SELECT n FROM Notice n " +
            "WHERE (:searchType IS NULL OR " +
            "       (:searchType = 'TITLE' AND n.title LIKE %:keyword%) OR " +
            "       (:searchType = 'TITLE_CONTENT' AND (n.title LIKE %:keyword% OR n.content LIKE %:keyword%))) " +
            "AND (:from IS NULL OR n.createdAt >= :from) " +
            "AND (:to IS NULL OR n.createdAt <= :to) " +
            "AND :today BETWEEN n.startAt AND n.endAt")
    Page<Notice> searchNotices(
            @Param("searchType") String searchType,
            @Param("keyword") String keyword,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to,
            @Param("today") LocalDateTime today,
            Pageable pageable);

}
