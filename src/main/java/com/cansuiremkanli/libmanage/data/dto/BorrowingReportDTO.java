package com.cansuiremkanli.libmanage.data.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class BorrowingReportDTO {
    private UUID borrowingId;
    private String userName;
    private String userEmail;
    private String bookTitle;
    private LocalDate dueDate;
}
