package com.cansuiremkanli.libmanage.data.dto;

import lombok.Data;

@Data
public class BorrowingStatsDTO {
    private long totalBorrowings;
    private long activeBorrowings; // returnDate == null
    private long overdueCount;
    private double overduePercentage;
}
