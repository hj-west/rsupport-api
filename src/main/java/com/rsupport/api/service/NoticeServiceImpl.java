package com.rsupport.api.service;

import com.rsupport.api.dto.NoticeListResponseDto;
import com.rsupport.api.dto.enums.SearchType;
import com.rsupport.api.entity.Attachment;
import com.rsupport.api.entity.Notice;
import com.rsupport.api.entity.User;
import com.rsupport.api.repository.AttachmentRepository;
import com.rsupport.api.repository.NoticeRepository;
import com.rsupport.api.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

    @Override
    public Page<NoticeListResponseDto> getNotices(SearchType searchType, String keyword, LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return noticeRepository.searchNotices(Optional.ofNullable(searchType).map(Enum::toString).orElse(null)
                        , keyword
                        , from
                        , to
                        , LocalDateTime.now()
                        , pageable)
                .map(NoticeListResponseDto::new);
    }

    @Override
    public void saveNotice(String title, String content, LocalDateTime startAt, LocalDateTime endAt, List<MultipartFile> files) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new IllegalArgumentException("세션에서 사용자 정보를 찾을 수 없습니다.");
        }

        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            throw new IllegalArgumentException("사용자 정보를 찾을 수 없습니다.");
        }
        Notice notice = Notice.builder()
                .title(title)
                .content(content)
                .startAt(startAt)
                .endAt(endAt)
                .author(user)
                .viewCount(0)
                .build();

        noticeRepository.save(notice);

        if (files != null) {
            List<Attachment> attachments = files.stream()
                    .map(file -> {
                        return new Attachment(null, file.getOriginalFilename(), fileService.upload(file), notice);
                    })
                    .collect(Collectors.toList());

            attachmentRepository.saveAll(attachments);
            notice.setAttachments(attachments);
        }
    }

    @Override
    public void updateNotice(Long id, String title, String content, LocalDateTime startAt, LocalDateTime endAt, List<MultipartFile> files) {
        Notice notice = noticeRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다."));

        // 수정기능엔 @NotBlank 가 적용되지 않으므로 null체크를 통해 비어있으면 기존값으로 세팅되도록 설정
        Optional.ofNullable(title).filter(s -> !title.isEmpty()).ifPresent(notice::setTitle);
        Optional.ofNullable(content).filter(s -> !content.isEmpty()).ifPresent(notice::setContent);
        Optional.ofNullable(startAt).ifPresent(notice::setStartAt);
        Optional.ofNullable(endAt).ifPresent(notice::setEndAt);
        noticeRepository.save(notice);

        if (files != null) {
            // 기존 첨부파일 삭제
            attachmentRepository.deleteAll(notice.getAttachments());
            notice.getAttachments().clear();

            // 새로운 첨부파일 추가
            List<Attachment> attachments = files.stream()
                    .map(file -> new Attachment(null, file.getOriginalFilename(), fileService.upload(file), notice))
                    .collect(Collectors.toList());

            attachmentRepository.saveAll(attachments);
            notice.setAttachments(attachments);
        }
    }
}
