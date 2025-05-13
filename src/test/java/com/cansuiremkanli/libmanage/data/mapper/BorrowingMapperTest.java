
package com.cansuiremkanli.libmanage.data.mapper;

import com.cansuiremkanli.libmanage.data.dto.BorrowingDTO;
import com.cansuiremkanli.libmanage.data.entity.Book;
import com.cansuiremkanli.libmanage.data.entity.Borrowing;
import com.cansuiremkanli.libmanage.data.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BorrowingMapperTest {

    private final BorrowingMapper borrowingMapper = Mappers.getMapper(BorrowingMapper.class);

    @Test
    void testToDTO() {
        User user = new User();
        user.setId(UUID.randomUUID());

        Book book = new Book();
        book.setId(UUID.randomUUID());

        Borrowing borrowing = new Borrowing();
        borrowing.setUser(user);
        borrowing.setBook(book);
        borrowing.setBorrowDate(LocalDate.now());
        borrowing.setDueDate(LocalDate.now().plusWeeks(2));

        BorrowingDTO dto = borrowingMapper.toDTO(borrowing);

        assertThat(dto.getUserId()).isEqualTo(user.getId());
        assertThat(dto.getBookId()).isEqualTo(book.getId());
    }
}
