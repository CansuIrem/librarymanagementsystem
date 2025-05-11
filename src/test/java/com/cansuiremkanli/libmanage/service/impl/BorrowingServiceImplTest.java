
package com.cansuiremkanli.libmanage.service.impl;

import com.cansuiremkanli.libmanage.data.dto.BorrowingDTO;
import com.cansuiremkanli.libmanage.data.dto.BorrowingReportDTO;
import com.cansuiremkanli.libmanage.data.dto.BorrowingStatsDTO;
import com.cansuiremkanli.libmanage.data.entity.Book;
import com.cansuiremkanli.libmanage.data.entity.Borrowing;
import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.mapper.BorrowingMapper;
import com.cansuiremkanli.libmanage.data.repository.BookRepository;
import com.cansuiremkanli.libmanage.data.repository.BorrowingRepository;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BorrowingServiceImplTest {

    @InjectMocks
    private BorrowingServiceImpl borrowingService;

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowingMapper borrowingMapper;

    private UUID userId;
    private UUID bookId;
    private User user;
    private Book book;
    private Borrowing borrowing;
    private BorrowingDTO borrowingDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        bookId = UUID.randomUUID();
        user = new User();
        book = new Book();
        book.setAvailableCount(1);
        borrowing = new Borrowing();
        borrowingDTO = new BorrowingDTO();
    }

    @Test
    void testBorrowBook_success() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.save(any())).thenReturn(book);
        when(borrowingRepository.save(any())).thenReturn(borrowing);
        when(borrowingMapper.toDTO(any())).thenReturn(borrowingDTO);

        BorrowingDTO result = borrowingService.borrowBook(userId, bookId);

        assertEquals(borrowingDTO, result);
    }

    @Test
    void testReturnBook_success() {
        UUID borrowingId = UUID.randomUUID();
        borrowing.setBorrowDate(LocalDate.now().minusWeeks(2));
        borrowing.setDueDate(LocalDate.now().minusDays(1));
        borrowing.setBook(book);
        when(borrowingRepository.findById(borrowingId)).thenReturn(Optional.of(borrowing));
        when(borrowingRepository.save(any())).thenReturn(borrowing);
        when(borrowingMapper.toDTO(borrowing)).thenReturn(borrowingDTO);

        BorrowingDTO result = borrowingService.returnBook(borrowingId);

        assertEquals(borrowingDTO, result);
    }

    @Test
    void testGetUserBorrowingHistory() {
        when(borrowingRepository.findByUserId(userId)).thenReturn(List.of(borrowing));
        when(borrowingMapper.toDTO(borrowing)).thenReturn(borrowingDTO);

        List<BorrowingDTO> result = borrowingService.getUserBorrowingHistory(userId);

        assertEquals(1, result.size());
    }

    @Test
    void testGetAllOverdueBooks() {
        when(borrowingRepository.findByIsOverdueTrue()).thenReturn(List.of(borrowing));
        when(borrowingMapper.toDTO(borrowing)).thenReturn(borrowingDTO);

        List<BorrowingDTO> result = borrowingService.getAllOverdueBooks();

        assertEquals(1, result.size());
    }
    @Test
    void testUpdateOverdueStatuses_marksOverdueCorrectly() {
        Borrowing notReturnedAndPastDue = new Borrowing();
        notReturnedAndPastDue.setId(UUID.randomUUID());
        notReturnedAndPastDue.setDueDate(LocalDate.now().minusDays(1));
        notReturnedAndPastDue.setReturnDate(null);
        notReturnedAndPastDue.setOverdue(false);

        when(borrowingRepository.findAll()).thenReturn(List.of(notReturnedAndPastDue));
        when(borrowingRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        borrowingService.updateOverdueStatuses();

        ArgumentCaptor<Borrowing> captor = ArgumentCaptor.forClass(Borrowing.class);
        verify(borrowingRepository).save(captor.capture());

        assertTrue(captor.getValue().isOverdue());
    }

    @Test
    void testGetOverdueReport_returnsProperDTOs() {
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");

        Book book = new Book();
        book.setTitle("Test Book");

        Borrowing borrowing = new Borrowing();
        borrowing.setId(UUID.randomUUID());
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setDueDate(LocalDate.now().plusDays(3));
        borrowing.setOverdue(true);

        when(borrowingRepository.findByIsOverdueTrue()).thenReturn(List.of(borrowing));

        List<BorrowingReportDTO> report = borrowingService.getOverdueReport();

        assertEquals(1, report.size());
        assertEquals("Test User", report.get(0).getUserName());
        assertEquals("Test Book", report.get(0).getBookTitle());
    }

    @Test
    void testGetBorrowingStats_returnsCorrectValues() {
        Borrowing b1 = new Borrowing();
        b1.setReturnDate(null);
        b1.setOverdue(true);

        Borrowing b2 = new Borrowing();
        b2.setReturnDate(LocalDate.now());
        b2.setOverdue(false);

        when(borrowingRepository.findAll()).thenReturn(List.of(b1, b2));

        BorrowingStatsDTO stats = borrowingService.getBorrowingStats();

        assertEquals(2, stats.getTotalBorrowings());
        assertEquals(1, stats.getActiveBorrowings());
        assertEquals(1, stats.getOverdueCount());
        assertEquals(50.0, stats.getOverduePercentage());
    }
}
