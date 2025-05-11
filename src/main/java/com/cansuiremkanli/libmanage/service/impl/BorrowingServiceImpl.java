package com.cansuiremkanli.libmanage.service.impl;

import com.cansuiremkanli.libmanage.data.dto.BorrowingReportDTO;
import com.cansuiremkanli.libmanage.data.dto.BorrowingStatsDTO;
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
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowingServiceImpl implements BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final BorrowingMapper borrowingMapper;

    @Override
    public BorrowingDTO borrowBook(UUID userId, UUID bookId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new EntityNotFoundException("Book not found"));

        if (book.getAvailableCount() <= 0) {
            throw new IllegalStateException("Book is not available for borrowing.");
        }

        long activeBorrowings = borrowingRepository.countByUserIdAndReturnDateIsNull(userId);
        if (activeBorrowings >= 5) {
            throw new IllegalStateException("User has reached the maximum number of active borrowings.");
        }

        boolean hasOverdue = borrowingRepository.existsByUserIdAndIsOverdueTrue(userId);
        if (hasOverdue) {
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

        return borrowingMapper.toDTO(borrowing);
    }


    @Override
    public BorrowingDTO returnBook(UUID borrowingId) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new EntityNotFoundException("Borrowing not found"));

        if (borrowing.getReturnDate() != null) {
            throw new IllegalStateException("Book already returned");
        }

        borrowing.setReturnDate(LocalDate.now());
        borrowing.setOverdue(LocalDate.now().isAfter(borrowing.getDueDate()));

        Book book = borrowing.getBook();
        book.setAvailableCount(book.getAvailableCount() + 1);

        bookRepository.save(book);
        borrowingRepository.save(borrowing);

        return borrowingMapper.toDTO(borrowing);
    }

    @Override
    public List<BorrowingDTO> getUserBorrowingHistory(UUID userId) {
        return borrowingRepository.findByUserId(userId)
                .stream()
                .map(borrowingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BorrowingDTO> getAllOverdueBooks() {
        return borrowingRepository.findByIsOverdueTrue()
                .stream()
                .map(borrowingMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void updateOverdueStatuses() {
        List<Borrowing> borrowings = borrowingRepository.findAll();

        for (Borrowing borrowing : borrowings) {
            boolean shouldBeOverdue = borrowing.getReturnDate() == null
                    && borrowing.getDueDate().isBefore(LocalDate.now());

            if (shouldBeOverdue && !borrowing.isOverdue()) {
                borrowing.setOverdue(true);
                borrowingRepository.save(borrowing);
            }
        }
    }
    @Override
    public List<BorrowingReportDTO> getOverdueReport() {
        return borrowingRepository.findByIsOverdueTrue()
                .stream()
                .map(borrowing -> {
                    BorrowingReportDTO dto = new BorrowingReportDTO();
                    dto.setBorrowingId(borrowing.getId());
                    dto.setUserName(borrowing.getUser().getName());
                    dto.setUserEmail(borrowing.getUser().getEmail());
                    dto.setBookTitle(borrowing.getBook().getTitle());
                    dto.setDueDate(borrowing.getDueDate());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public BorrowingStatsDTO getBorrowingStats() {
        List<Borrowing> all = borrowingRepository.findAll();
        long total = all.size();
        long active = all.stream().filter(b -> b.getReturnDate() == null).count();
        long overdue = all.stream().filter(Borrowing::isOverdue).count();
        double percentage = total == 0 ? 0 : (double) overdue / total * 100;

        BorrowingStatsDTO stats = new BorrowingStatsDTO();
        stats.setTotalBorrowings(total);
        stats.setActiveBorrowings(active);
        stats.setOverdueCount(overdue);
        stats.setOverduePercentage(Math.round(percentage * 100.0) / 100.0);
        return stats;
    }


}
