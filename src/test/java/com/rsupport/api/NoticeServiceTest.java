package com.rsupport.api;

import com.rsupport.api.dto.NoticeDetailResponseDto;
import com.rsupport.api.dto.NoticeRequestDto;
import com.rsupport.api.entity.Notice;
import com.rsupport.api.entity.User;
import com.rsupport.api.repository.AttachmentRepository;
import com.rsupport.api.repository.NoticeRepository;
import com.rsupport.api.repository.UserRepository;
import com.rsupport.api.service.FileService;
import com.rsupport.api.service.NoticeServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private FileService fileService;

    @Mock
    private HttpSession session;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

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

    @Test
    @DisplayName("공지 저장 테스트 1. 저장 성공")
    void testSaveNotice_Success() {
        Mockito.lenient().when(session.getAttribute("userId")).thenReturn(1L);
        Mockito.lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        NoticeRequestDto requestDto = new NoticeRequestDto("New Title", "New Content",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L, List.of());

        when(noticeRepository.save(any(Notice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> noticeService.saveNotice(
                requestDto.getTitle(), requestDto.getContent(), requestDto.getStartAt(), requestDto.getEndAt(), requestDto.getFiles()
        ));
    }

    @Test
    @DisplayName("공지 저장 테스트 2. 세션에 사용자 정보가 없음")
    void testSaveNotice_DoNotFindUserIdInSession() {
        Mockito.lenient().when(session.getAttribute("userId")).thenReturn(1L);

        NoticeRequestDto requestDto = new NoticeRequestDto("New Title", "New Content",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L, List.of());

        assertThrows(IllegalArgumentException.class, () -> noticeService.saveNotice(
                requestDto.getTitle(), requestDto.getContent(), requestDto.getStartAt(), requestDto.getEndAt(), requestDto.getFiles()
        ));
    }

    @Test
    @DisplayName("공지 저장 테스트 2. 세션의 사용자 정보를 바탕으로 사용자를 찾을 수 없음")
    void testSaveNotice_DoNotFoundUser() {
        Mockito.lenient().when(session.getAttribute("userId")).thenReturn(2L);
        Mockito.lenient().when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        NoticeRequestDto requestDto = new NoticeRequestDto("New Title", "New Content",
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1L, List.of());

        assertThrows(IllegalArgumentException.class, () -> noticeService.saveNotice(
                requestDto.getTitle(), requestDto.getContent(), requestDto.getStartAt(), requestDto.getEndAt(), requestDto.getFiles()
        ));
    }

    @Test
    @DisplayName("공지 조회 테스트 1. 조회 성공")
    void testGetNotice_Success() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNotice));

        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // Redis Mock 설정
        when(valueOperations.increment(anyString(), anyLong())).thenReturn(1L); // 조회수 증가 처리 Mocking

        NoticeDetailResponseDto response = noticeService.getNotice(1L);
        assertNotNull(response);
        assertEquals("Test Title", response.getTitle());
    }

    @Test
    @DisplayName("공지 조회 테스트 2. 존재하지 않는 공지 조회")
    void testGetNotice_DoNotFoundNotice() {
        lenient().when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNotice));
        when(noticeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> noticeService.getNotice(2L));
    }

    @Test
    @DisplayName("공지 수정 테스트 1. 수정 성공")
    void testUpdateNotice_Success() {
        when(noticeRepository.findById(1L)).thenReturn(Optional.of(testNotice));

        assertDoesNotThrow(() -> noticeService.updateNotice(1L, "Updated Title", "Updated Content", LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of()));
    }

    @Test
    @DisplayName("공지 수정 테스트 2. 존재하지 않는 공지 수정")
    void testUpdateNotice_DoNotFoundNotice() {
        when(noticeRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->  noticeService.updateNotice(2L, "Updated Title", "Updated Content", LocalDateTime.now(), LocalDateTime.now().plusDays(1), List.of()));
    }
}
