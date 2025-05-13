package com.cansuiremkanli.libmanage.service.impl;

import com.cansuiremkanli.libmanage.data.entity.Book;
import com.cansuiremkanli.libmanage.data.entity.Borrowing;
import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.repository.BookRepository;
import com.cansuiremkanli.libmanage.data.repository.BorrowingRepository;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import com.cansuiremkanli.libmanage.data.dto.BorrowingDTO;
import com.cansuiremkanli.libmanage.data.mapper.BorrowingMapper;
import com.cansuiremkanli.libmanage.service.BorrowingService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowingMapper borrowingMapper;

    @Override
    public BorrowingDTO borrowBook(UUID userId, UUID bookId) {
        log.info("Borrow request: userId={}, bookId={}", userId, bookId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.warn("User not found with ID: {}", userId);
                    return new EntityNotFoundException("User not found");
                });

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    log.warn("Book not found with ID: {}", bookId);
                    return new EntityNotFoundException("Book not found");
                });

        if (book.getAvailableCount() <= 0) {
            log.warn("Book [{}] is not available for borrowing", bookId);
            throw new IllegalStateException("Book is not available for borrowing.");
        }

        long activeBorrowings = borrowingRepository.countByUserIdAndReturnDateIsNull(userId);
        if (activeBorrowings >= 5) {
            log.warn("User [{}] exceeded active borrowing limit", userId);
            throw new IllegalStateException("User has reached the maximum number of active borrowings.");
        }

        boolean hasOverdue = borrowingRepository.existsByUserIdAndIsOverdueTrue(userId);
        if (hasOverdue) {
            log.warn("User [{}] has overdue books", userId);
            throw new IllegalStateException("User has overdue books and cannot borrow new ones.");
        }

        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setDueDate(LocalDate.now().plusWeeks(2));
        borrowing.setOverdue(false);

        book.setAvailableCount(book.getAvailableCount() - 1);

        bookRepository.save(book);
        borrowingRepository.save(borrowing);

        log.info("Borrowing created successfully for user [{}] and book [{}]", userId, bookId);

        return borrowingMapper.toDTO(borrowing);
    }

    @Override
    public BorrowingDTO returnBook(UUID borrowingId) {
        log.info("Return request for borrowingId={}", borrowingId);

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> {
                    log.warn("Borrowing not found with ID: {}", borrowingId);
                    return new EntityNotFoundException("Borrowing not found");
                });

        if (borrowing.getReturnDate() != null) {
            log.warn("Attempted to return already returned borrowingId={}", borrowingId);
            throw new IllegalStateException("Book already returned");
        }

        borrowing.setReturnDate(LocalDate.now());
        borrowing.setOverdue(LocalDate.now().isAfter(borrowing.getDueDate()));

        Book book = borrowing.getBook();
        book.setAvailableCount(book.getAvailableCount() + 1);

        bookRepository.save(book);
        borrowingRepository.save(borrowing);

        log.info("Book returned successfully. borrowingId={}, overdue={}", borrowingId, borrowing.isOverdue());

        return borrowingMapper.toDTO(borrowing);
    }

    @Override
    public List<BorrowingDTO> getUserBorrowingHistory(UUID userId) {
        log.info("Fetching borrowing history for user [{}]", userId);
        return borrowingRepository.findByUserId(userId)
                .stream()
                .map(borrowingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowingDTO> getAllOverdueBooks() {
        log.info("Fetching all overdue borrowings");
        return borrowingRepository.findByIsOverdueTrue()
                .stream()
                .map(borrowingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateOverdueStatuses() {
        log.info("Updating overdue statuses for all borrowings");
        List<Borrowing> borrowings = borrowingRepository.findAll();

        int updatedCount = 0;
        for (Borrowing borrowing : borrowings) {
            boolean shouldBeOverdue = borrowing.getReturnDate() == null
                    && borrowing.getDueDate().isBefore(LocalDate.now());

            if (shouldBeOverdue && !borrowing.isOverdue()) {
                borrowing.setOverdue(true);
                borrowingRepository.save(borrowing);
                updatedCount++;
            }
        }

        log.info("Overdue status update completed. Updated {} borrowings.", updatedCount);
    }

    @Override
    public String getOverdueReport() {
        List<Borrowing> overdueBorrowings = borrowingRepository.findByIsOverdueTrue();

        StringBuilder report = new StringBuilder();
        report.append("OVERDUE BORROWING REPORT\n")
                .append("----------------------------\n")
                .append("Total Overdue: ").append(overdueBorrowings.size()).append("\n\n");

        for (Borrowing borrowing : overdueBorrowings) {
            report.append("Borrowing ID: ").append(borrowing.getId()).append("\n")
                    .append("User: ").append(borrowing.getUser().getName())
                    .append(" (").append(borrowing.getUser().getEmail()).append(")\n")
                    .append("Book Title: ").append(borrowing.getBook().getTitle()).append("\n")
                    .append("Due Date: ").append(borrowing.getDueDate()).append("\n")
                    .append("----------------------------\n");
        }

        report.append("Last Updated: ").append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return report.toString();
    }


    @Override
    public String getBorrowingStats() {
        List<Borrowing> all = borrowingRepository.findAll();
        long total = all.size();
        long active = all.stream().filter(b -> b.getReturnDate() == null).count();
        long overdue = all.stream().filter(Borrowing::isOverdue).count();
        double percentage = total == 0 ? 0 : (double) overdue / total * 100;

        return """
            BORROWING STATISTICS REPORT
            ---------------------------
            Total Borrowings: %d
            Active Borrowings: %d
            Overdue Borrowings: %d
            Overdue Percentage: %.2f%%
            
            Last Updated: %s
            """.formatted(
                total,
                active,
                overdue,
                Math.round(percentage * 100.0) / 100.0,
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        );
    }

}
