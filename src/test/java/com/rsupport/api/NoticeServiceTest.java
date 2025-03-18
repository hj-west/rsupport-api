package com.rsupport.api;

import com.rsupport.api.entity.Notice;
import com.rsupport.api.entity.User;
import com.rsupport.api.service.NoticeServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @InjectMocks
    private NoticeServiceImpl noticeService;

    private Notice testNotice;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User(1L, "Test User");

        testNotice = Notice.builder()
                .id(1L)
                .title("Test Title")
                .content("Test Content")
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(1))
                .attachments(Collections.emptyList())
                .author(mockUser)
                .build();
        ;
    }
}
