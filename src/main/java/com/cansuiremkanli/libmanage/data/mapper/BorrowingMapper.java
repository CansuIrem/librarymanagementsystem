package com.cansuiremkanli.libmanage.data.mapper;

import com.cansuiremkanli.libmanage.data.entity.Borrowing;
import com.cansuiremkanli.libmanage.data.dto.BorrowingDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BorrowingMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "book.id", target = "bookId")
    BorrowingDTO toDTO(Borrowing borrowing);

    @Mapping(source = "userId", target = "user.id")
    @Mapping(source = "bookId", target = "book.id")
    Borrowing toEntity(BorrowingDTO borrowingDTO);
}
