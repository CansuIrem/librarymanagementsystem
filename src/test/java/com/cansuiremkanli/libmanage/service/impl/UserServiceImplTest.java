
package com.cansuiremkanli.libmanage.service.impl;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.dto.UserCreateDTO;
import com.cansuiremkanli.libmanage.data.dto.UserDTO;
import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.mapper.UserMapper;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;


    private User user;
    private UserDTO userDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        userDTO = new UserDTO();
        userDTO.setId(userId);
    }

    @Test
    void testCreateUser() {
        // GIVEN
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setName("Test User");
        userCreateDTO.setEmail("test@example.com");
        userCreateDTO.setPhoneNumber("05551112233");
        userCreateDTO.setRole(Role.PATRON);
        userCreateDTO.setPassword("rawPassword");

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPhoneNumber("05551112233");
        user.setRole(Role.PATRON);
        user.setPassword("encodedPassword");

        UserDTO expectedDTO = new UserDTO();
        expectedDTO.setId(user.getId());
        expectedDTO.setName("Test User");
        expectedDTO.setEmail("test@example.com");
        expectedDTO.setPhoneNumber("05551112233");
        expectedDTO.setRole(Role.PATRON);

        // WHEN
        when(userMapper.toEntity(userCreateDTO)).thenReturn(user);
        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(expectedDTO);

        // THEN
        UserDTO result = userService.createUser(userCreateDTO);

        assertNotNull(result);
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        assertEquals("05551112233", result.getPhoneNumber());
        assertEquals(Role.PATRON, result.getRole());
    }




    @Test
    void testGetUserById_found() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDTO);

        UserDTO result = userService.getUserById(userId);

        assertEquals(userDTO, result);
    }

    @Test
    void testGetUserById_notFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void testGetAllUsers() {
        List<User> users = List.of(user);
        List<UserDTO> userDTOs = List.of(userDTO);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDtoList(users)).thenReturn(userDTOs); // DOĞRU mocking

        List<UserDTO> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(userDTOs.getFirst(), result.getFirst());
    }


    @Test
    void testUpdateUser() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDTO);

        UserDTO result = userService.updateUser(userId, userDTO);

        assertEquals(userDTO, result);
        verify(userRepository).save(user);
    }


    @Test
    void testDeleteUser_WhenUserExists_ShouldCallDelete() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void testDeleteUser_WhenUserDoesNotExist_ShouldThrowException() {
        UUID userId = UUID.randomUUID();

        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(userId));

        verify(userRepository, never()).deleteById(any());
    }
}
