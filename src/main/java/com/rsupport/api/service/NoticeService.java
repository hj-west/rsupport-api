package com.rsupport.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface NoticeService {
    void saveNotice(String title, String content, LocalDateTime startAt, LocalDateTime endAt, List<MultipartFile> files);
    void updateNotice(Long id, String title, String content, LocalDateTime startAt, LocalDateTime endAt, List<MultipartFile> files);
}
