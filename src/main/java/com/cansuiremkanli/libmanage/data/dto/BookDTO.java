package com.cansuiremkanli.libmanage.data.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.*;

public class BookDTO {
    private UUID id;

    @NotBlank
    private String title;

    @NotBlank
    private String author;

    @Pattern(regexp = "\\d{13}", message = "ISBN must be 13 digits")
    private String isbn;

    @NotBlank
    private String genre;

    @PastOrPresent
    private LocalDate publicationDate;

    @Min(0)
    private int availableCount;

    @Min(0)
    private int totalCount;
}


