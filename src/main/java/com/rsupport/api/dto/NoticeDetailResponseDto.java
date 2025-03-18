package com.rsupport.api.dto;

import com.rsupport.api.entity.Attachment;
import com.rsupport.api.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class NoticeDetailResponseDto {
    private Long id;
    private String title;
    private String content;
    private String author;
    private LocalDateTime createdAt;
    private Integer viewCount;
    private List<String> attachmentUrls;

    public NoticeDetailResponseDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.content = notice.getContent();
        this.author = notice.getAuthor().getUsername();
        this.createdAt = notice.getCreatedAt();
        this.viewCount = notice.getViewCount();
        this.attachmentUrls = notice.getAttachments().stream().map(Attachment::getFileUrl).collect(Collectors.toList());
    }
}
