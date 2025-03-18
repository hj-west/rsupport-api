package com.rsupport.api.service;

import com.rsupport.api.entity.Attachment;
import com.rsupport.api.entity.Notice;
import com.rsupport.api.entity.User;
import com.rsupport.api.repository.AttachmentRepository;
import com.rsupport.api.repository.NoticeRepository;
import com.rsupport.api.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
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
}
