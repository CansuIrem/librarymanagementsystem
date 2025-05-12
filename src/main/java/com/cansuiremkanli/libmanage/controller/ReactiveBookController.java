package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.publisher.BookStockPublisher;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/reactive/books")
@RequiredArgsConstructor
@Tag(name = "Reactive Book Management", description = "Operations related to reactive book management")
public class ReactiveBookController {

    private final BookStockPublisher bookStockPublisher;

    @PreAuthorize("hasAnyRole('LIBRARIAN', 'PATRON')")
    @Operation(summary = "Get all books with reactive", description = "Retrieves a list of all books.")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<BookDTO> streamBookUpdates() {
        return bookStockPublisher.getBookStream();
    }
}

