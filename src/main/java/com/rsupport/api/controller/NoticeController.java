package com.rsupport.api.controller;

import com.rsupport.api.dto.NoticeRequestDto;
import com.rsupport.api.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> createNotice(@ModelAttribute NoticeRequestDto request) {
        try {
            noticeService.saveNotice(request.getTitle(), request.getContent(), request.getStartAt(), request.getEndAt(), request.getFiles());
            return ResponseEntity.status(HttpStatus.CREATED).body("공지사항 저장을 성공했습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }

    }
}
