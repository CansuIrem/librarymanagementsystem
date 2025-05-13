
package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.BorrowingDTO;
import com.cansuiremkanli.libmanage.service.BorrowingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
        String dummyReport = """
            OVERDUE BORROWING REPORT
            ----------------------------
            Total Overdue: 1

            Borrowing ID: 1234
            User: Test User (test@example.com)
            Book Title: Test Book
            Due Date: 2025-05-15
            ----------------------------
            Last Updated: 2025-05-13T12:00:00
            """;

        Mockito.when(borrowingService.getOverdueReport()).thenReturn(dummyReport);

        mockMvc.perform(get("/api/borrowings/overdue/report"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("OVERDUE BORROWING REPORT")))
                .andExpect(content().string(containsString("Total Overdue: 1")));
    }


    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testGetBorrowingStats() throws Exception {
        String dummyReport = """
        BORROWING STATISTICS REPORT
        ---------------------------
        Total Borrowings: 10
        Active Borrowings: 4
        Overdue Borrowings: 2
        Overdue Percentage: 20.00%
        
        Last Updated: 2025-05-13T12:00:00
        """;

        Mockito.when(borrowingService.getBorrowingStats()).thenReturn(dummyReport);

        mockMvc.perform(get("/api/borrowings/stats"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BORROWING STATISTICS REPORT")))
                .andExpect(content().string(containsString("Total Borrowings: 10")))
                .andExpect(content().string(containsString("Overdue Percentage: 20.00%")));
    }

}
