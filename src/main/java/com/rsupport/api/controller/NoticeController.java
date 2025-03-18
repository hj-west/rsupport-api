package com.rsupport.api.controller;

import com.rsupport.api.dto.NoticeDetailResponseDto;
import com.rsupport.api.dto.NoticeListResponseDto;
import com.rsupport.api.dto.NoticeRequestDto;
import com.rsupport.api.dto.enums.SearchType;
import com.rsupport.api.dto.validation.RegisterRequestValidationGroup;
import com.rsupport.api.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
class NoticeController {
    private final NoticeService noticeService;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final String DEFAULT_SORT_FIELD = "createdAt";

    /**
     * 공지사할 목록 조회 API
     * @param searchType 검색 타입
     * @param keyword 검색어
     * @param from 조회시작일
     * @param to 조회종료일
     * @param page 페이지번호
     * @param size 페이지당 공지글 갯수
     * @param sort 정렬값 조건
     * @return ResponseEntity<Page<NoticeListResponseDto>>
     */
    @GetMapping
    public ResponseEntity<Page<NoticeListResponseDto>> getNotices(@RequestParam(required = false) SearchType searchType,
                                                                  @RequestParam(required = false) String keyword,
                                                                  @RequestParam(required = false) LocalDateTime from,
                                                                  @RequestParam(required = false) LocalDateTime to,
                                                                  @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
                                                                  @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size,
                                                                  @RequestParam(defaultValue = DEFAULT_SORT_FIELD) String sort,
                                                                  @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(noticeService.getNotices(searchType, keyword, from, to, pageable));
    }

    /**
     * 공지사항 상세조회 API 생성
     * @param noticeId 공지사항 id
     * @return ResponseEntity<NoticeDetailResponseDto>
     */
    @GetMapping("/{noticeId}")
    public ResponseEntity<NoticeDetailResponseDto> getNotice(@PathVariable String noticeId) {
        return ResponseEntity.ok(noticeService.getNotice(Long.valueOf(noticeId)));
    }

    /**
     * 공지사항 저장 API
     * @param request NoticeRequestDto
     * @return ResponseEntity<String>
     */
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<String> createNotice(@ModelAttribute @Validated(RegisterRequestValidationGroup.class) NoticeRequestDto request) {
        noticeService.saveNotice(request.getTitle(), request.getContent(), request.getStartAt(), request.getEndAt(), request.getFiles());
        return ResponseEntity.ok("공지사항 저장 성공");
    }

    /**
     * 공지사항 수정 API
     * @param request NoticeRequestDto
     * @param noticeId 공지사항 id
     * @return ResponseEntity<String>
     */
    @PutMapping(path = "/{noticeId}", consumes = { "multipart/form-data" })
    public ResponseEntity<String> updateNotice(@ModelAttribute @Valid NoticeRequestDto request, @PathVariable Long noticeId) {
        noticeService.updateNotice(noticeId, request.getTitle(), request.getContent(), request.getStartAt(), request.getEndAt(), request.getFiles());
        return ResponseEntity.ok("공지사항 수정 성공");
    }
}
