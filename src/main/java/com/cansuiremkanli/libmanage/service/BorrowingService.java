package com.cansuiremkanli.libmanage.service;

import com.cansuiremkanli.libmanage.data.dto.BorrowingDTO;

import java.util.List;
import java.util.UUID;

public interface BorrowingService {
    BorrowingDTO borrowBook(UUID userId, UUID bookId);
    BorrowingDTO returnBook(UUID borrowingId);
    List<BorrowingDTO> getUserBorrowingHistory(UUID userId);
    List<BorrowingDTO> getAllOverdueBooks();
    void updateOverdueStatuses();
    String getOverdueReport();
    String getBorrowingStats();


}
