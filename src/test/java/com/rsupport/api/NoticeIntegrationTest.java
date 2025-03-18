package com.rsupport.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rsupport.api.dto.NoticeRequestDto;
import com.rsupport.api.entity.Notice;
import com.rsupport.api.entity.User;
import com.rsupport.api.repository.NoticeRepository;
import com.rsupport.api.repository.UserRepository;
import com.rsupport.api.service.NoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class NoticeIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    private MockHttpSession session;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;


    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
        session.setAttribute("userId", 1L);
    }

    @Test
    @DisplayName("공지 목록 조회 API 테스트 1. 파라미터 없이 정상조회")
    void testGetNotices_Success() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notices")
                        .session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("공지 목록 조회 API 테스트 2. 파라미터가 넘어오는 상태에서의 정상조회")
    void testGetNotices_WithParameters() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notices")
                        .param("searchType", "TITLE")
                        .param("keyword", "Test")
                        .param("page", "1")
                        .param("size", "5")
                        .session(session))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("공지 목록 조회 API 테스트 3. 잘못된 파라미터")
    void testGetNotices_InvalidPageParameter() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notices")
                        .param("page", "-1")
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공지 상세 조회 API 테스트 1. 정상 조회")
    void testGetNotice_Success() throws Exception {
        Notice notice = Notice.builder()
                .title("Test Title")
                .content("Test Content")
                .author(userRepository.findById(1L).orElse(new User(1L, "admin")))
                .startAt(LocalDateTime.now().minusDays(3))
                .endAt(LocalDateTime.now().plusDays(3))
                .attachments(new ArrayList<>())
                .viewCount(0)
                .build();

        notice = noticeRepository.save(notice);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/notices/" + notice.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(notice.getId()))
                .andExpect(jsonPath("$.title").value("Test Title"))
                .andExpect(jsonPath("$.content").value("Test Content"))
                .andExpect(jsonPath("$.author").value("admin"));
    }

    @Test
    @DisplayName("공지 상세 조회 API 테스트 2. 잘못된 notice id")
    void testGetNotice_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/notices/9999")
                        .session(session))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공지 등록 API 테스트 1. 정상 등록")
    void testCreateNotice_Success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "Test content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/notices")
                        .file(file)
                        .param("title", "Title")
                        .param("content", "Content")
                        .param("startAt", LocalDateTime.now().toString())
                        .param("endAt", LocalDateTime.now().plusDays(1).toString())
                        .session(session)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("공지 등록 API 테스트 2. 필수데이터 누락")
    void testCreateNotice_InvalidRequest() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/notices")
                        .param("title", "")
                        .param("content", "")
                        .param("startAt", "")
                        .param("endAt", "")
                        .session(session)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
    @Test
    @DisplayName("공지 수정 API 테스트 1. 수정 성공")
    void testUpdateNotice_Success() throws Exception {
        Notice notice = Notice.builder()
                .title("Test Title")
                .content("Test Content")
                .author(userRepository.findById(1L).orElse(new User(1L, "admin")))
                .startAt(LocalDateTime.now().minusDays(3))
                .endAt(LocalDateTime.now().plusDays(3))
                .attachments(new ArrayList<>())
                .viewCount(0)
                .build();

        noticeRepository.save(notice);

        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "Test content".getBytes());

        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/api/notices/1")
                        .file(file)
                        .param("title", "Title")
                        .param("content", "Content")
                        .param("startAt", LocalDateTime.now().toString())
                        .param("endAt", LocalDateTime.now().plusDays(1).toString())
                        .session(session)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }


    @Test
    @DisplayName("공지 수정 API 테스트 2. 빈 값이 들어와도 수정은 성공(기존값 사용)")
    void testUpdateNotice_EmptyRequest_Success() throws Exception {
        Notice notice = Notice.builder()
                .title("Test Title")
                .content("Test Content")
                .author(userRepository.findById(1L).orElse(new User(1L, "admin")))
                .startAt(LocalDateTime.now().minusDays(3))
                .endAt(LocalDateTime.now().plusDays(3))
                .attachments(new ArrayList<>())
                .viewCount(0)
                .build();

        noticeRepository.save(notice);
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/api/notices/1")
                        .param("title", "")
                        .param("content", "")
                        .param("startAt", "")
                        .param("endAt", "")
                        .session(session)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("공지 수정 API 테스트 3. 잘못된 notice id")
    void testUpdateNotice_NotFound() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.multipart(HttpMethod.PUT, "/api/notices/9999")
                        .param("title", "")
                        .param("content", "")
                        .param("startAt", "")
                        .param("endAt", "")
                        .session(session)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }
}
