package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "Operations related to book management")
public class BookController {

    private final BookService bookService;

    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Add a Book", description = "Adds a new book to the library")
    @PostMapping
    public ResponseEntity<BookDTO> addBook(@Valid @RequestBody BookDTO bookDTO) {
        log.info("Requested to add a new book: {}", bookDTO.getTitle());
        return ResponseEntity.ok(bookService.addBook(bookDTO));
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Delete a Book", description = "Deletes a book from the library by ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable UUID id) {
        log.warn("Requested to delete book with ID: {}", id);
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'PATRON')")
    @Operation(summary = "Search Books", description = "Searches books by title, author, ISBN, or genre with pagination")
    @GetMapping("/search")
    public ResponseEntity<Page<BookDTO>> searchBooks(
            @RequestParam String keyword,
            @RequestParam String type,
            Pageable pageable) {
        log.info("Search requested for books. Keyword: '{}', Type: '{}'", keyword, type);
        return ResponseEntity.ok(bookService.searchBooks(keyword, type, pageable));
    }

    // Aşağıdaki işlemler zaten servis katmanında loglanacağı için burada ayrıca loglama gerekmez

    @PreAuthorize("hasRole('LIBRARIAN')")
    @Operation(summary = "Update a Book", description = "Updates book information by ID")
    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateBook(@PathVariable UUID id, @Valid @RequestBody BookDTO bookDTO) {
        return ResponseEntity.ok(bookService.updateBook(id, bookDTO));
    }

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'PATRON')")
    @Operation(summary = "Get Book Details", description = "Retrieves details of a book by ID")
    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> getBookById(@PathVariable UUID id) {
        return ResponseEntity.ok(bookService.getBookById(id));
    }
}
