package com.cansuiremkanli.libmanage.data.mapper;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.data.entity.Book;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BookMapperTest {

    private final BookMapper bookMapper = Mappers.getMapper(BookMapper.class);

    @Test
    void testToEntityAndToDTO() {
        BookDTO dto = new BookDTO();
        dto.setTitle("Test Book");
        dto.setAuthor("Author");
        dto.setIsbn("1234567890123");
        dto.setGenre("Novel");
        dto.setAvailableCount(10);
        dto.setTotalCount(15);

        Book entity = bookMapper.toEntity(dto);
        BookDTO mappedBack = bookMapper.toDTO(entity);

        assertEquals(dto.getTitle(), mappedBack.getTitle());
        assertEquals(dto.getAuthor(), mappedBack.getAuthor());
        assertEquals(dto.getIsbn(), mappedBack.getIsbn());
        assertEquals(dto.getGenre(), mappedBack.getGenre());
        assertEquals(dto.getAvailableCount(), mappedBack.getAvailableCount());
        assertEquals(dto.getTotalCount(), mappedBack.getTotalCount());
    }
}
