package com.cansuiremkanli.libmanage.data.repository;

import com.cansuiremkanli.libmanage.data.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Book> findByAuthorContainingIgnoreCase(String author, Pageable pageable);

    Page<Book> findByIsbnContainingIgnoreCase(String isbn, Pageable pageable);

    Page<Book> findByGenreContainingIgnoreCase(String genre, Pageable pageable);
}

