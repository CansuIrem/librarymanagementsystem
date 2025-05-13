package com.cansuiremkanli.libmanage.data.mapper;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.data.entity.Book;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BookMapper {

    Book toEntity(BookDTO dto);

    BookDTO toDTO(Book entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(BookDTO dto, @MappingTarget Book entity);
}

