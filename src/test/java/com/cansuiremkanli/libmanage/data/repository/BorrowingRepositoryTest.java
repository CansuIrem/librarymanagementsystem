
package com.cansuiremkanli.libmanage.data.repository;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.entity.Book;
import com.cansuiremkanli.libmanage.data.entity.Borrowing;
import com.cansuiremkanli.libmanage.data.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
@DataJpaTest
class BorrowingRepositoryTest {

    @Autowired
    private BorrowingRepository borrowingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Test
    void testFindByUserIdAndIsOverdueTrue() {
        // Veritabanına önce User ve Book kaydedilmeli
        User user = new User();
        user.setName("Irem Kanli");
        user.setEmail("iremkanli@mail.com");
        user.setPhoneNumber("5555555555");
        user.setRole(Role.PATRON);

        Book book = new Book();
        book.setTitle("Sample Book");
        book.setAuthor("Author Name");
        book.setIsbn("1234567890123");
        book.setGenre("Fiction");
        book.setAvailableCount(1);
        book.setTotalCount(1);

        user = userRepository.save(user);
        book = bookRepository.save(book);

        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(LocalDate.now().minusWeeks(3));
        borrowing.setDueDate(LocalDate.now().minusDays(1));
        borrowing.setOverdue(true);

        borrowingRepository.save(borrowing);

        List<Borrowing> overdueList = borrowingRepository.findByIsOverdueTrue();
        assertThat(overdueList).isNotEmpty();
    }
}
