package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.BorrowingDTO;
import com.cansuiremkanli.libmanage.data.dto.BorrowingReportDTO;
import com.cansuiremkanli.libmanage.data.dto.BorrowingStatsDTO;
import com.cansuiremkanli.libmanage.service.BorrowingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
@Tag(name = "Borrowing Management", description = "Handles borrowing and returning of books")
public class BorrowingController {

    private final BorrowingService borrowingService;

    @PreAuthorize("hasRole('PATRON')")
    @PostMapping("/borrow")
    @Operation(summary = "Borrow a book", description = "Patrons borrow an available book")
    public ResponseEntity<BorrowingDTO> borrowBook(
            @RequestParam UUID userId,
            @RequestParam UUID bookId) {
        return ResponseEntity.ok(borrowingService.borrowBook(userId, bookId));
    }

    @PreAuthorize("hasRole('PATRON')")
    @PostMapping("/return/{borrowingId}")
    @Operation(summary = "Return a book", description = "Patrons return a previously borrowed book")
    public ResponseEntity<BorrowingDTO> returnBook(@PathVariable UUID borrowingId) {
        return ResponseEntity.ok(borrowingService.returnBook(borrowingId));
    }

    @PreAuthorize("hasRole('LIBRARIAN') or #userId == principal.id")
    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user borrowing history", description = "Lists all borrowings for a user")
    public ResponseEntity<List<BorrowingDTO>> getUserHistory(@PathVariable UUID userId) {
        return ResponseEntity.ok(borrowingService.getUserBorrowingHistory(userId));
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue books", description = "Lists all overdue borrowings in the system")
    public ResponseEntity<List<BorrowingDTO>> getOverdueBooks() {
        return ResponseEntity.ok(borrowingService.getAllOverdueBooks());
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/overdue/report")
    @Operation(summary = "Overdue Borrowings Report", description = "Lists all overdue borrowings with user and book details")
    public ResponseEntity<List<BorrowingReportDTO>> getOverdueReport() {
        return ResponseEntity.ok(borrowingService.getOverdueReport());
    }

    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/stats")
    @Operation(summary = "Borrowing Statistics", description = "Provides statistics about all borrowings in the system")
    public ResponseEntity<BorrowingStatsDTO> getBorrowingStats() {
        return ResponseEntity.ok(borrowingService.getBorrowingStats());
    }

}
