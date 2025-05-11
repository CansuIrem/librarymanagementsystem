
package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.service.BookService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(roles = "LIBRARIAN")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    private ObjectMapper objectMapper;
    private BookDTO bookDTO;
    private UUID bookId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        bookId = UUID.randomUUID(); // <-- NULL OLMAMALI

        bookDTO = new BookDTO();
        bookDTO.setTitle("Recaizade Mahmut Ekrem");
        bookDTO.setAuthor("Araba Sevdası");
        bookDTO.setIsbn("9780134685991");
        bookDTO.setGenre("Novel");
        bookDTO.setAvailableCount(5);
        bookDTO.setTotalCount(5);
    }


    @Test
    void testAddBook() throws Exception {
        Mockito.when(bookService.addBook(any())).thenReturn(bookDTO);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateBook() throws Exception {
        Mockito.when(bookService.updateBook(eq(bookId), any())).thenReturn(bookDTO);

        mockMvc.perform(put("/api/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookDTO)))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteBook() throws Exception {
        mockMvc.perform(delete("/api/books/" + bookId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testGetBookById() throws Exception {
        Mockito.when(bookService.getBookById(bookId)).thenReturn(bookDTO);

        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isOk());
    }

    @Test
    void testSearchBooks() throws Exception {
        Page<BookDTO> page = new PageImpl<>(List.of(bookDTO));
        Mockito.when(bookService.searchBooks(anyString(), anyString(), any())).thenReturn(page);

        mockMvc.perform(get("/api/books/search")
                        .param("keyword", "Java")
                        .param("type", "title"))
                .andExpect(status().isOk());
    }
}
