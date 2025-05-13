
package com.cansuiremkanli.libmanage.data.mapper;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.dto.UserDTO;
import com.cansuiremkanli.libmanage.data.entity.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void testToDtoAndToEntity() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test");
        user.setEmail("test@example.com");
        user.setPhoneNumber("123456789");
        user.setRole(Role.PATRON);

        UserDTO dto = userMapper.toDto(user);
        assertThat(dto.getName()).isEqualTo(user.getName());

        User mapped = userMapper.toEntity(dto);
        assertThat(mapped.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    void testToDtoList() {
        User user = new User();
        user.setName("List User");

        List<UserDTO> dtoList = userMapper.toDtoList(List.of(user));
        assertThat(dtoList).hasSize(1);
        assertThat(dtoList.get(0).getName()).isEqualTo("List User");
    }
}
