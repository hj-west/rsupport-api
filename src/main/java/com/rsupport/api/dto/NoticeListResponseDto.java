package com.rsupport.api.dto;

import com.rsupport.api.entity.Notice;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class NoticeListResponseDto {
    private Long id;
    private String title;
    private String author;
    private LocalDateTime createdAt;
    private Integer viewCount;
    private Boolean hasAttachment;

    public NoticeListResponseDto(Notice notice) {
        this.id = notice.getId();
        this.title = notice.getTitle();
        this.author = notice.getAuthor().getUsername();
        this.createdAt = notice.getCreatedAt();
        this.viewCount = notice.getViewCount();
        this.hasAttachment = !notice.getAttachments().isEmpty();
    }
}
