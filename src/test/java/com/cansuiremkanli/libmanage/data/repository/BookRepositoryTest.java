
package com.cansuiremkanli.libmanage.data.repository;

import com.cansuiremkanli.libmanage.data.entity.Book;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class BookRepositoryTest {

    @Autowired
    private BookRepository bookRepository;

    @Test
    void testFindByTitleContainingIgnoreCase() {
        Book book = new Book();
        book.setTitle("Spring Boot");
        bookRepository.save(book);

        Page<Book> result = bookRepository.findByTitleContainingIgnoreCase("spring", PageRequest.of(0, 10));
        assertThat(result.getContent()).isNotEmpty();
    }
}
