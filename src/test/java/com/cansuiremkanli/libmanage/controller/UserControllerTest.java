
package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.dto.UserDTO;
import com.cansuiremkanli.libmanage.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private ObjectMapper objectMapper;
    private UserDTO userDTO;
    private UUID userId;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();

        userId = UUID.randomUUID();
        userDTO = new UserDTO();
        userDTO.setName("Irem Kanlı");
        userDTO.setEmail("iremkanli@mail.com");
        userDTO.setPhoneNumber("5555555555");
        userDTO.setRole(Role.PATRON);
    }


    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testCreateUser() throws Exception {
        Mockito.when(userService.createUser(any())).thenReturn(userDTO);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testGetUserById() throws Exception {
        Mockito.when(userService.getUserById(userId)).thenReturn(userDTO);

        mockMvc.perform(get("/api/users/" + userId))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testGetAllUsers() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(List.of(userDTO));

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testUpdateUser() throws Exception {
        Mockito.when(userService.updateUser(eq(userId), any())).thenReturn(userDTO);

        mockMvc.perform(put("/api/users/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isOk());
    }

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/" + userId))
                .andExpect(status().isNoContent());
    }
}
