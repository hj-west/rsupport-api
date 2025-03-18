package com.rsupport.api;

import com.rsupport.api.repository.NoticeRepository;
import com.rsupport.api.service.NoticeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

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
}
