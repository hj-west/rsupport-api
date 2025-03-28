package com.rsupport.api.service;

import com.rsupport.api.dto.NoticeDetailResponseDto;
import com.rsupport.api.dto.NoticeListResponseDto;
import com.rsupport.api.dto.enums.SearchType;
import com.rsupport.api.entity.Attachment;
import com.rsupport.api.entity.Notice;
import com.rsupport.api.entity.User;
import com.rsupport.api.repository.AttachmentRepository;
import com.rsupport.api.repository.NoticeRepository;
import com.rsupport.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
    private final HttpSession session;

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final AttachmentRepository attachmentRepository;

    private final FileService fileService;
    private final StringRedisTemplate redisTemplate;

    private static final String VIEW_KEY_PREFIX = "notice:view:";

    private Long getCurrentUserId() {
        return Optional.ofNullable((Long) session.getAttribute("userId"))
                .orElseThrow(() -> new IllegalArgumentException("세션에서 사용자 정보를 찾을 수 없습니다."));
    }

    @Override
    public Page<NoticeListResponseDto> getNotices(SearchType searchType, String keyword, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return noticeRepository.searchNotices(Optional.ofNullable(searchType).map(Enum::toString).orElse(null), keyword, from, to, LocalDateTime.now(), pageable)
                .map(NoticeListResponseDto::new);
    }

    @Override
    public NoticeDetailResponseDto getNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));
        redisTemplate.opsForValue().increment(VIEW_KEY_PREFIX + id, 1);
        return new NoticeDetailResponseDto(notice);
    }

    @Override
    @Transactional
    public void saveNotice(String title, String content, LocalDateTime startAt, LocalDateTime endAt, List<MultipartFile> files) {
        User user = userRepository.findById(getCurrentUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        Notice notice = noticeRepository.save(Notice.builder()
                .title(title)
                .content(content)
                .startAt(startAt)
                .endAt(endAt)
                .author(user)
                .viewCount(0)
                .build());

        if (files != null && !files.isEmpty()) {
            List<Attachment> attachments = files.stream()
                    .map(file -> new Attachment(null, file.getOriginalFilename(), fileService.upload(file), notice))
                    .collect(Collectors.toList());
            attachmentRepository.saveAll(attachments);
            notice.setAttachments(attachments);
        }
    }

    @Override
    @Transactional
    public void updateNotice(Long id, String title, String content, LocalDateTime startAt, LocalDateTime endAt, List<MultipartFile> files) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));

        Optional.ofNullable(title).filter(s -> !s.isEmpty()).ifPresent(notice::setTitle);
        Optional.ofNullable(content).filter(s -> !s.isEmpty()).ifPresent(notice::setContent);
        Optional.ofNullable(startAt).ifPresent(notice::setStartAt);
        Optional.ofNullable(endAt).ifPresent(notice::setEndAt);

        if (files != null && !files.isEmpty()) {
            // 기존 첨부파일 컬렉션의 참조를 유지한 채 내용을 교체
            notice.getAttachments().clear(); // 기존 첨부파일 삭제(삭제쿼리 실행)
            List<Attachment> attachments = files.stream()
                    .map(file -> new Attachment(null, file.getOriginalFilename(), fileService.upload(file), notice))
                    .toList();
            notice.getAttachments().addAll(attachments); // 기존 컬렉션에 추가
        }
    }


    @Override
    public void deleteNotice(Long id) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));

        // 첨부 파일 삭제
        notice.getAttachments().forEach(attachment -> {
            fileService.delete(attachment.getFileUrl());
        });
        attachmentRepository.deleteAll(notice.getAttachments());

        // 공지사항 삭제
        noticeRepository.delete(notice);
    }

    /**
     * 일정 주기로 Redis의 조회수를 DB로 반영하는 메서드
     */
    @Scheduled(fixedRate = 600000) // 10분마다 실행 (600,000ms = 10분)
    @Transactional
    public void syncViewCountsToDB() {
        Set<String> keys = redisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys.isEmpty()) return;

        for (String key : keys) {
            String noticeIdStr = key.replace(VIEW_KEY_PREFIX, "");
            Long noticeId = Long.parseLong(noticeIdStr);
            Integer viewCount = Integer.valueOf(Objects.requireNonNull(redisTemplate.opsForValue().get(key)));
            noticeRepository.findById(noticeId).ifPresent(notice -> {
                notice.setViewCount(notice.getViewCount() + viewCount);
                noticeRepository.save(notice);
            });
            // Redis에서 해당 조회수 키 삭제
            redisTemplate.delete(key);
        }
    }
}
