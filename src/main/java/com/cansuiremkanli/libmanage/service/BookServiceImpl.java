package com.cansuiremkanli.libmanage.service;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.data.entity.Book;
import com.cansuiremkanli.libmanage.data.mapper.BookMapper;
import com.cansuiremkanli.libmanage.data.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    @Override
    public BookDTO addBook(BookDTO bookDTO) {
        Book book = bookMapper.toEntity(bookDTO);
        return bookMapper.toDTO(bookRepository.save(book));
    }

    @Override
    public BookDTO updateBook(UUID id, BookDTO bookDTO) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found"));

        bookMapper.updateEntity(bookDTO, book);
        return bookMapper.toDTO(bookRepository.save(book));
    }

    @Override
    public void deleteBook(UUID id) {
        bookRepository.deleteById(id);
    }

    @Override
    public BookDTO getBookById(UUID id) {
        return bookRepository.findById(id)
                .map(bookMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Book not found"));
    }

    @Override
    public Page<BookDTO> searchBooks(String keyword, String type, Pageable pageable) {
        switch (type.toLowerCase()) {
            case "title":
                return bookRepository.findByTitleContainingIgnoreCase(keyword, pageable)
                        .map(bookMapper::toDTO);
            case "author":
                return bookRepository.findByAuthorContainingIgnoreCase(keyword, pageable)
                        .map(bookMapper::toDTO);
            case "isbn":
                return bookRepository.findByIsbnContainingIgnoreCase(keyword, pageable)
                        .map(bookMapper::toDTO);
            case "genre":
                return bookRepository.findByGenreContainingIgnoreCase(keyword, pageable)
                        .map(bookMapper::toDTO);
            default:
                throw new IllegalArgumentException("Invalid search type");
        }
    }
}
