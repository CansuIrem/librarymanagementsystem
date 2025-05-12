package com.cansuiremkanli.libmanage.service.impl;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.data.entity.Book;
import com.cansuiremkanli.libmanage.data.mapper.BookMapper;
import com.cansuiremkanli.libmanage.data.repository.BookRepository;
import com.cansuiremkanli.libmanage.publisher.BookStockPublisher;
import com.cansuiremkanli.libmanage.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookStockPublisher bookStockPublisher;

    @Override
    public BookDTO addBook(BookDTO bookDTO) {
        log.info("Adding new book: {}", bookDTO.getTitle());
        Book book = bookMapper.toEntity(bookDTO);
        Book saved = bookRepository.save(book);
        log.info("Book added successfully with ID: {}", saved.getId());
        return bookMapper.toDTO(saved);
    }

    @Override
    public BookDTO updateBook(UUID id, BookDTO bookDTO) {
        log.info("Updating book with ID: {}", id);

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Book not found with ID: {}", id);
                    return new RuntimeException("Book not found with id: " + id);
                });

        book.setAvailableCount(bookDTO.getAvailableCount());
        Book updated = bookRepository.save(book);

        log.info("Book updated successfully: ID={}, AvailableCount={}", updated.getId(), updated.getAvailableCount());

        BookDTO updatedDTO = bookMapper.toDTO(updated);
        bookStockPublisher.publish(updatedDTO);
        log.debug("Published updated book stock event for book ID: {}", updatedDTO.getId());

        return updatedDTO;
    }

    @Override
    public void deleteBook(UUID id) {
        log.warn("Deleting book with ID: {}", id);
        bookRepository.deleteById(id);
        log.info("Book deleted successfully: {}", id);
    }

    @Override
    public BookDTO getBookById(UUID id) {
        log.info("Fetching book details for ID: {}", id);
        return bookRepository.findById(id)
                .map(book -> {
                    log.info("Book found: {}", book.getTitle());
                    return bookMapper.toDTO(book);
                })
                .orElseThrow(() -> {
                    log.warn("Book not found with ID: {}", id);
                    return new RuntimeException("Book not found");
                });
    }

    @Override
    public Page<BookDTO> searchBooks(String keyword, String type, Pageable pageable) {
        log.info("Searching books. Keyword: '{}', Type: '{}'", keyword, type);

        return switch (type.toLowerCase()) {
            case "title" -> {
                log.debug("Searching by title...");
                yield bookRepository.findByTitleContainingIgnoreCase(keyword, pageable).map(bookMapper::toDTO);
            }
            case "author" -> {
                log.debug("Searching by author...");
                yield bookRepository.findByAuthorContainingIgnoreCase(keyword, pageable).map(bookMapper::toDTO);
            }
            case "isbn" -> {
                log.debug("Searching by ISBN...");
                yield bookRepository.findByIsbnContainingIgnoreCase(keyword, pageable).map(bookMapper::toDTO);
            }
            case "genre" -> {
                log.debug("Searching by genre...");
                yield bookRepository.findByGenreContainingIgnoreCase(keyword, pageable).map(bookMapper::toDTO);
            }
            default -> {
                log.error("Invalid search type: {}", type);
                throw new IllegalArgumentException("Invalid search type");
            }
        };
    }
}
