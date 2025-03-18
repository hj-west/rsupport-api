package com.rsupport.api.service;

import com.rsupport.api.dto.NoticeDetailResponseDto;
import com.rsupport.api.dto.NoticeListResponseDto;
import com.rsupport.api.dto.enums.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public interface NoticeService {
    Page<NoticeListResponseDto> getNotices(SearchType searchType, String keyword, LocalDateTime from, LocalDateTime to, Pageable pageable);
    NoticeDetailResponseDto getNotice(Long id);
    void saveNotice(String title, String content, LocalDateTime startAt, LocalDateTime endAt, List<MultipartFile> files);
    void updateNotice(Long id, String title, String content, LocalDateTime startAt, LocalDateTime endAt, List<MultipartFile> files);
    void deleteNotice(Long id);
}
