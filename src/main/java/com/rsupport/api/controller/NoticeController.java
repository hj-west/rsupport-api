package com.rsupport.api.controller;

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
                                                                  @RequestParam(defaultValue = "0") int page,
                                                                  @RequestParam(defaultValue = "10") int size,
                                                                  @RequestParam(defaultValue = "createdAt") String sort,
                                                                  @RequestParam(defaultValue = "DESC") Sort.Direction sortDirection) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        return ResponseEntity.ok(noticeService.getNotices(searchType, keyword, from, to, pageable));
    }

    /**
     * 공지사항 저장 API
     * @param request NoticeRequestDto
     * @return ResponseEntity<String>
     */
    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<String> createNotice(@ModelAttribute @Validated(RegisterRequestValidationGroup.class) NoticeRequestDto request) {
        try {
            noticeService.saveNotice(request.getTitle(), request.getContent(), request.getStartAt(), request.getEndAt(), request.getFiles());
            return ResponseEntity.status(HttpStatus.CREATED).body("공지사항 저장을 성공했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }

    /**
     * 공지사항 수정 API
     * @param request NoticeRequestDto
     * @param noticeId 공지사항 id
     * @return ResponseEntity<String>
     */
    @PutMapping(path = "/{noticeId}", consumes = { "multipart/form-data" })
    public ResponseEntity<String> updateNotice(@ModelAttribute @Valid NoticeRequestDto request, @PathVariable String noticeId) {
        try {
            noticeService.updateNotice(Long.valueOf(noticeId), request.getTitle(), request.getContent(), request.getStartAt(), request.getEndAt(), request.getFiles());
            return ResponseEntity.status(HttpStatus.CREATED).body("공지사항 수정을 성공했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
