package com.cansuiremkanli.libmanage.integration;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.data.dto.RegisterRequest;
import com.cansuiremkanli.libmanage.data.repository.BookRepository;
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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        bookRepository.deleteAll();
    }

    private String registerAndGetToken(String email, String role) throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail(email);
        request.setPassword("password123");
        request.setPhoneNumber("05551112233");
        request.setRole(Role.valueOf(role));

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).get("token").asText();
    }

    private BookDTO createSampleBookDTO() {
        BookDTO book = new BookDTO();
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setIsbn("1234567890123");
        book.setGenre("Science");
        book.setPublicationDate(LocalDate.of(2020, 1, 1));
        book.setAvailableCount(3);
        book.setTotalCount(5);
        return book;
    }

    @Test
    void addBook_WithLibrarianRole_ShouldSucceed() throws Exception {
        String token = registerAndGetToken("lib@book.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO();

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void addBook_WithInvalidISBN_ShouldReturn400() throws Exception {
        String token = registerAndGetToken("lib@invalid.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO();
        book.setIsbn("invalidISBN");

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.isbn").value("ISBN must be 13 digits"));
    }

    @Test
    void addBook_WithBlankTitle_ShouldReturn400() throws Exception {
        String token = registerAndGetToken("lib@blank.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO();
        book.setTitle("");

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("must not be blank"));
    }

    @Test
    void addBook_WithNegativeAvailableCount_ShouldReturn400() throws Exception {
        String token = registerAndGetToken("lib@negcount.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO();
        book.setAvailableCount(-1);

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.availableCount").value("must be greater than or equal to 0"));
    }

    @Test
    void addBook_WithPatronRole_ShouldReturn403() throws Exception {
        String token = registerAndGetToken("patron@book.com", "PATRON");
        BookDTO book = createSampleBookDTO();

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Access denied. Access Denied"));
    }

    @Test
    void getBookById_ShouldReturnBook() throws Exception {
        String token = registerAndGetToken("librarian@get.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO();

        MvcResult result = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(get("/api/books/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"));
    }

    @Test
    void updateBook_ShouldUpdateWhenCountsAreValid() throws Exception {
        String token = registerAndGetToken("updatevalid@book.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO(); // örn. available=3, total=5

        // Kitap ekleniyor
        MvcResult result = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // Geçerli güncelleme: availableCount <= totalCount
        book.setAvailableCount(4);
        book.setTotalCount(5);

        mockMvc.perform(put("/api/books/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableCount").value(4));
    }

    @Test
    void updateBook_ShouldFailWhenAvailableCountExceedsTotalCount() throws Exception {
        String token = registerAndGetToken("updateinvalid@book.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO(); // örn. available=3, total=5

        // Kitap ekleniyor
        MvcResult result = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        // Hatalı güncelleme: availableCount > totalCount
        book.setAvailableCount(10);
        book.setTotalCount(5);

        mockMvc.perform(put("/api/books/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Available count cannot be greater than total count")));
    }


    @Test
    void deleteBook_ShouldReturn204() throws Exception {
        String token = registerAndGetToken("delete@book.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO();

        MvcResult result = mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andReturn();

        UUID id = UUID.fromString(objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText());

        mockMvc.perform(delete("/api/books/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void searchBooks_ByTitle_ShouldReturnResults() throws Exception {
        String token = registerAndGetToken("search@book.com", "LIBRARIAN");
        BookDTO book = createSampleBookDTO();
        book.setTitle("Test Book");

        mockMvc.perform(post("/api/books")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "Test")
                        .param("type", "title")
                        .param("size", "10")
                        .param("page", "0")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Test Book"));
    }
}