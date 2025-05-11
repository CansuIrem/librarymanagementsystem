package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@Tag(name = "Book Controller", description = "Operations related to book management")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Add a Book", description = "Adds a new book to the library")
    @PostMapping
    public BookDTO addBook(@RequestBody BookDTO bookDTO) {
        return bookService.addBook(bookDTO);
    }

    @Operation(summary = "Update a Book", description = "Updates book information by ID")
    @PutMapping("/{id}")
    public BookDTO updateBook(@PathVariable UUID id, @RequestBody BookDTO bookDTO) {
        return bookService.updateBook(id, bookDTO);
    }

    @Operation(summary = "Delete a Book", description = "Deletes a book from the library by ID")
    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
    }

    @Operation(summary = "Get Book Details", description = "Retrieves details of a book by ID")
    @GetMapping("/{id}")
    public BookDTO getBookById(@PathVariable UUID id) {
        return bookService.getBookById(id);
    }

    @Operation(summary = "Search Books", description = "Searches books by title, author, ISBN, or genre with pagination")
    @GetMapping("/search")
    public Page<BookDTO> searchBooks(
            @RequestParam String keyword,
            @RequestParam String type,
            Pageable pageable) {
        return bookService.searchBooks(keyword, type, pageable);
    }
}
