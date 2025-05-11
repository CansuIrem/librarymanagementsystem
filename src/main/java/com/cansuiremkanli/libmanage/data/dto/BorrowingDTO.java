package com.cansuiremkanli.libmanage.data.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BorrowingDTO {
    private UUID id;
    private UUID userId;
    private UUID bookId;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean isOverdue;
}
