
package com.cansuiremkanli.libmanage.service.impl;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.data.entity.Book;
import com.cansuiremkanli.libmanage.data.mapper.BookMapper;
import com.cansuiremkanli.libmanage.data.repository.BookRepository;
import com.cansuiremkanli.libmanage.publisher.BookStockPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BookServiceImplTest {

    @InjectMocks
    private BookServiceImpl bookService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private BookStockPublisher bookStockPublisher;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddBook() {
        BookDTO dto = new BookDTO();
        Book entity = new Book();

        when(bookMapper.toEntity(dto)).thenReturn(entity);
        when(bookRepository.save(entity)).thenReturn(entity);
        when(bookMapper.toDTO(entity)).thenReturn(dto);

        BookDTO result = bookService.addBook(dto);

        assertEquals(dto, result);
        verify(bookRepository).save(entity);
    }

    @Test
    void testUpdateBook() {
        UUID id = UUID.randomUUID();

        BookDTO inputDto = new BookDTO();
        inputDto.setTitle("Updated Title");

        Book bookEntity = new Book();
        bookEntity.setId(id);
        bookEntity.setTitle("Updated Title");

        BookDTO expectedDto = new BookDTO();
        expectedDto.setId(id);
        expectedDto.setTitle("Updated Title");

        when(bookRepository.findById(id)).thenReturn(Optional.of(new Book()));
        when(bookMapper.toEntity(inputDto)).thenReturn(bookEntity);
        when(bookRepository.save(bookEntity)).thenReturn(bookEntity);
        when(bookMapper.toDTO(bookEntity)).thenReturn(expectedDto);
        doNothing().when(bookStockPublisher).publish(expectedDto);

        BookDTO result = bookService.updateBook(id, inputDto);

        assertEquals(expectedDto, result);
    }


    @Test
    void testDeleteBook() {
        UUID id = UUID.randomUUID();
        bookService.deleteBook(id);
        verify(bookRepository).deleteById(id);
    }

    @Test
    void testGetBookById_found() {
        UUID id = UUID.randomUUID();
        Book book = new Book();
        BookDTO dto = new BookDTO();

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookMapper.toDTO(book)).thenReturn(dto);

        BookDTO result = bookService.getBookById(id);

        assertEquals(dto, result);
    }

    @Test
    void testGetBookById_notFound() {
        UUID id = UUID.randomUUID();
        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> bookService.getBookById(id));
    }

    @Test
    void testSearchBooks_byTitle() {
        String keyword = "Java";
        String type = "title";
        Pageable pageable = mock(Pageable.class);

        Book book = new Book();
        BookDTO bookDTO = new BookDTO();

        when(bookRepository.findByTitleContainingIgnoreCase(keyword, pageable))
                .thenReturn(new PageImpl<>(List.of(book)));
        when(bookMapper.toDTO(book)).thenReturn(bookDTO);

        Page<BookDTO> result = bookService.searchBooks(keyword, type, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testSearchBooks_invalidType() {
        assertThrows(IllegalArgumentException.class,
                () -> bookService.searchBooks("test", "unknown", mock(Pageable.class)));
    }
}
