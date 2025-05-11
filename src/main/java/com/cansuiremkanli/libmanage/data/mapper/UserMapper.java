package com.cansuiremkanli.libmanage.data.mapper;

import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.dto.UserDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserDTO toDto(User user);
    User toEntity(UserDTO userDTO);
    List<UserDTO> toDtoList(List<User> users);
}

