package com.cansuiremkanli.libmanage.integration;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.dto.RegisterRequest;
import com.cansuiremkanli.libmanage.data.entity.Book;
import com.cansuiremkanli.libmanage.data.entity.Borrowing;
import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.repository.BookRepository;
import com.cansuiremkanli.libmanage.data.repository.BorrowingRepository;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import com.cansuiremkanli.libmanage.service.BorrowingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BorrowingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private BorrowingRepository borrowingRepository;
    @Autowired private BorrowingService borrowingService;


    @BeforeEach
    void cleanUp() {
        borrowingRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String registerAndGetToken(String email, String role) throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail(email);
        req.setPassword("password123");
        req.setPhoneNumber("05551112233");
        req.setRole(Role.valueOf(role));

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private UUID createBook(String title, int availableCount) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor("Author");
        book.setIsbn(UUID.randomUUID().toString());
        book.setGenre("Fiction");
        book.setTotalCount(10);
        book.setAvailableCount(availableCount);
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        return bookRepository.save(book).getId();
    }

    @Test
    void borrowBook_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("patron1@example.com", "PATRON");
        UUID userId = userRepository.findByEmail("patron1@example.com").orElseThrow().getId();
        UUID bookId = createBook("Borrowable Book", 2);

        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", userId.toString())
                        .param("bookId", bookId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(userId.toString()))
                .andExpect(jsonPath("$.bookId").value(bookId.toString()));
    }

    @Test
    void borrowBook_WithNoStock_ShouldReturn400() throws Exception {
        String token = registerAndGetToken("patron2@example.com", "PATRON");
        UUID userId = userRepository.findByEmail("patron2@example.com").orElseThrow().getId();
        UUID bookId = createBook("Unavailable Book", 0);

        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", userId.toString())
                        .param("bookId", bookId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Book is not available for borrowing."));
    }

    @Test
    void returnBook_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("patron3@example.com", "PATRON");
        UUID userId = userRepository.findByEmail("patron3@example.com").orElseThrow().getId();
        UUID bookId = createBook("Returnable Book", 3);

        // borrow first
        MvcResult result = mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", userId.toString())
                        .param("bookId", bookId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        UUID borrowingId = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // then return
        mockMvc.perform(post("/api/borrowings/return/" + borrowingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returnDate").exists())
                .andExpect(jsonPath("$.overdue").value(false));
    }

    @Test
    void getUserBorrowingHistory_ShouldReturnList() throws Exception {
        String token = registerAndGetToken("patron4@example.com", "PATRON");
        UUID userId = userRepository.findByEmail("patron4@example.com").orElseThrow().getId();
        UUID bookId = createBook("History Book", 1);

        // borrow a book
        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", userId.toString())
                        .param("bookId", bookId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // get history
        mockMvc.perform(get("/api/borrowings/user/" + userId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOverdueBooks_AsLibrarian_ShouldReturnEmptyInitially() throws Exception {
        String token = registerAndGetToken("librarian@overdue.com", "LIBRARIAN");

        mockMvc.perform(get("/api/borrowings/overdue")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getOverdueReport_AsLibrarian_ShouldReturnEmptyInitially() throws Exception {
        String token = registerAndGetToken("librarian@report.com", "LIBRARIAN");

        mockMvc.perform(get("/api/borrowings/overdue/report")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("OVERDUE BORROWING REPORT")))
                .andExpect(content().string(containsString("Total Overdue: 0")));
    }


    @Test
    void getBorrowingStats_AsLibrarian_ShouldReturnTextReport() throws Exception {
        String token = registerAndGetToken("librarian@stats.com", "LIBRARIAN");

        mockMvc.perform(get("/api/borrowings/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("BORROWING STATISTICS REPORT")))
                .andExpect(content().string(containsString("Total Borrowings:")))
                .andExpect(content().string(containsString("Overdue Percentage:")));
    }


    @Test
    void borrowBook_WhenUserHasFiveActiveBorrowings_ShouldReturn409() throws Exception {
        String token = registerAndGetToken("limituser@example.com", "PATRON");
        User user = userRepository.findByEmail("limituser@example.com").orElseThrow();

        // 5 kitap oluştur ve ödünç ver
        for (int i = 0; i < 5; i++) {
            UUID bookId = createBook("Book" + i, 1);
            mockMvc.perform(post("/api/borrowings/borrow")
                            .param("userId", user.getId().toString())
                            .param("bookId", bookId.toString())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
        }

        // 6. kitap
        UUID sixthBookId = createBook("Book6", 1);
        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", user.getId().toString())
                        .param("bookId", sixthBookId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User has reached the maximum number of active borrowings."));
    }

    @Test
    void borrowBook_WhenUserHasOverdue_ShouldReturn409() throws Exception {
        String token = registerAndGetToken("overdueuser@example.com", "PATRON");
        User user = userRepository.findByEmail("overdueuser@example.com").orElseThrow();
        UUID bookId = createBook("Old Book", 1);

        Borrowing overdue = new Borrowing();
        overdue.setUser(user);
        overdue.setBook(bookRepository.findById(bookId).orElseThrow());
        overdue.setBorrowDate(LocalDate.now().minusWeeks(4));
        overdue.setDueDate(LocalDate.now().minusWeeks(2));
        overdue.setOverdue(true);
        borrowingRepository.save(overdue);

        UUID newBookId = createBook("New Book", 1);

        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", user.getId().toString())
                        .param("bookId", newBookId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("User has overdue books and cannot borrow new ones."));
    }

    @Test
    void borrowAndReturnSameBook_Twice_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("reuser@example.com", "PATRON");
        User user = userRepository.findByEmail("reuser@example.com").orElseThrow();
        UUID bookId = createBook("Reusable Book", 1);

        // 1. borrow
        MvcResult firstBorrow = mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", user.getId().toString())
                        .param("bookId", bookId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        UUID borrowingId = UUID.fromString(objectMapper.readTree(firstBorrow.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(post("/api/borrowings/return/" + borrowingId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // 2. borrow again
        mockMvc.perform(post("/api/borrowings/borrow")
                        .param("userId", user.getId().toString())
                        .param("bookId", bookId.toString())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void updateOverdueStatuses_ShouldMarkBorrowingAsOverdue() throws Exception {
        String token = registerAndGetToken("overduetester@example.com", "LIBRARIAN");
        User user = userRepository.findByEmail("overduetester@example.com").orElseThrow();
        UUID bookId = createBook("Overdue Test Book", 1);
        Book book = bookRepository.findById(bookId).orElseThrow();

        // Geçmiş tarihli (gecikmiş) borrowing oluştur
        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(LocalDate.now().minusWeeks(4));
        borrowing.setDueDate(LocalDate.now().minusWeeks(2));
        borrowing.setOverdue(false);
        borrowingRepository.save(borrowing);

        borrowingService.updateOverdueStatuses();

        Borrowing updated = borrowingRepository.findById(borrowing.getId()).orElseThrow();
        assertThat(updated.isOverdue()).isTrue();
    }

    @Test
    void getBorrowingStats_ShouldReturnCorrectReportText() throws Exception {
        String token = registerAndGetToken("statlib@example.com", "LIBRARIAN");
        User user = userRepository.findByEmail("statlib@example.com").orElseThrow();

        UUID book1 = createBook("Returned Book", 1);
        UUID book2 = createBook("Active Book", 1);
        UUID book3 = createBook("Overdue Book", 1);

        Book b1 = bookRepository.findById(book1).orElseThrow();
        Book b2 = bookRepository.findById(book2).orElseThrow();
        Book b3 = bookRepository.findById(book3).orElseThrow();

        // 1 - returned
        Borrowing br1 = new Borrowing();
        br1.setUser(user);
        br1.setBook(b1);
        br1.setBorrowDate(LocalDate.now().minusDays(10));
        br1.setDueDate(LocalDate.now().minusDays(3));
        br1.setReturnDate(LocalDate.now().minusDays(1));
        br1.setOverdue(false);

        // 2 - active
        Borrowing br2 = new Borrowing();
        br2.setUser(user);
        br2.setBook(b2);
        br2.setBorrowDate(LocalDate.now().minusDays(2));
        br2.setDueDate(LocalDate.now().plusDays(10));
        br2.setReturnDate(null);
        br2.setOverdue(false);

        // 3 - overdue
        Borrowing br3 = new Borrowing();
        br3.setUser(user);
        br3.setBook(b3);
        br3.setBorrowDate(LocalDate.now().minusWeeks(3));
        br3.setDueDate(LocalDate.now().minusWeeks(1));
        br3.setReturnDate(null);
        br3.setOverdue(true);

        borrowingRepository.save(br1);
        borrowingRepository.save(br2);
        borrowingRepository.save(br3);

        // İstatistik endpointi çağrılır
        MvcResult result = mockMvc.perform(get("/api/borrowings/stats")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();

        // İçerik kontrolü
        assertThat(responseBody).contains("BORROWING STATISTICS REPORT");
        assertThat(responseBody).contains("Total Borrowings: 3");
        assertThat(responseBody).contains("Active Borrowings: 2");
        assertThat(responseBody).contains("Overdue Borrowings: 1");
        assertThat(responseBody).contains("Overdue Percentage: 33.33%");
    }


}
