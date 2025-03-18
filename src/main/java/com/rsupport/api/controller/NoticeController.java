package com.rsupport.api.controller;

import com.rsupport.api.dto.NoticeRequestDto;
import com.rsupport.api.dto.validation.RegisterRequestValidationGroup;
import com.rsupport.api.service.NoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
class NoticeController {
    private final NoticeService noticeService;

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
