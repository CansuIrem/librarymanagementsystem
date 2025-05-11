
package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.BorrowingDTO;
import com.cansuiremkanli.libmanage.data.dto.BorrowingReportDTO;
import com.cansuiremkanli.libmanage.data.dto.BorrowingStatsDTO;
import com.cansuiremkanli.libmanage.service.BorrowingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BorrowingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BorrowingService borrowingService;

    private ObjectMapper objectMapper;
    private BorrowingDTO borrowingDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        borrowingDTO = new BorrowingDTO();
    }

    @WithMockUser(roles = "PATRON")
    @Test
    void testBorrowBook() throws Exception {
        Mockito.when(borrowingService.borrowBook(any(), any())).thenReturn(borrowingDTO);
        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", UUID.randomUUID().toString())
                        .param("bookId", UUID.randomUUID().toString()))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "PATRON")
    @Test
    void testReturnBook() throws Exception {
        Mockito.when(borrowingService.returnBook(any())).thenReturn(borrowingDTO);
        mockMvc.perform(post("/api/borrowings/return/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testGetUserHistory() throws Exception {
        Mockito.when(borrowingService.getUserBorrowingHistory(any())).thenReturn(List.of(borrowingDTO));
        mockMvc.perform(get("/api/borrowings/user/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testGetOverdueBooks() throws Exception {
        Mockito.when(borrowingService.getAllOverdueBooks()).thenReturn(List.of(borrowingDTO));
        mockMvc.perform(get("/api/borrowings/overdue"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testGetOverdueReport() throws Exception {
        Mockito.when(borrowingService.getOverdueReport()).thenReturn(List.of(new BorrowingReportDTO()));
        mockMvc.perform(get("/api/borrowings/overdue/report"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testGetBorrowingStats() throws Exception {
        Mockito.when(borrowingService.getBorrowingStats()).thenReturn(new BorrowingStatsDTO());
        mockMvc.perform(get("/api/borrowings/stats"))
                .andExpect(status().isOk());
    }
}
