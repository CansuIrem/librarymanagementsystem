package com.cansuiremkanli.libmanage.data.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "books")
@Data
public class Book extends BaseEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private String title;
    private String author;

    @Column(unique = true)
    private String isbn;

    private String genre;

    private int totalCount;
    private int availableCount;

    private LocalDate publicationDate;

    @OneToMany(mappedBy = "book")
    private Set<Borrowing> borrowings;
}

