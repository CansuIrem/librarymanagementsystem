package com.cansuiremkanli.libmanage.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.cansuiremkanli.libmanage.data.dto.BookDTO;

import java.util.UUID;

public interface BookService {

    BookDTO addBook(BookDTO bookDTO);

    BookDTO updateBook(UUID id, BookDTO bookDTO);

    void deleteBook(UUID id);

    BookDTO getBookById(UUID id);

    Page<BookDTO> searchBooks(String keyword, String type, Pageable pageable);
}
