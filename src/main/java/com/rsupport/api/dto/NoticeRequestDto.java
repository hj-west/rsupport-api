package com.rsupport.api.dto;


import com.rsupport.api.dto.validation.RegisterRequestValidationGroup;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class NoticeRequestDto {
    @NotBlank(groups = RegisterRequestValidationGroup.class)
    private String title;

    @NotBlank(groups = RegisterRequestValidationGroup.class)
    private String content;

    @NotBlank(groups = RegisterRequestValidationGroup.class)
    private LocalDateTime startAt;
    @NotBlank(groups = RegisterRequestValidationGroup.class)
    private LocalDateTime endAt;

    private Long authorId;

    private List<MultipartFile> files;

}
