package com.cansuiremkanli.libmanage.data.dto;

import java.time.LocalDate;
import java.util.UUID;

public class BookDTO {
    private UUID id;
    private String title;
    private String author;
    private String isbn;
    private String genre;
    private LocalDate publicationDate;
    private int availableCount;
    private int totalCount;
}

