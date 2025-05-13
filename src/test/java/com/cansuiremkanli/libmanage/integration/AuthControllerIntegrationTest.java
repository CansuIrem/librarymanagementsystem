package com.cansuiremkanli.libmanage.integration;

import com.cansuiremkanli.libmanage.data.dto.AuthenticationRequest;
import com.cansuiremkanli.libmanage.data.dto.RegisterRequest;
import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldCreateUserAndReturnJwtToken() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test User");
        request.setEmail("test@example.com");
        request.setPassword("123456");
        request.setPhoneNumber("555-1234");
        request.setRole(Role.LIBRARIAN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());

        assertThat(userRepository.findByEmail("test@example.com")).isPresent();
    }

    @Test
    void login_ShouldAuthenticateAndReturnJwtToken() throws Exception {
        RegisterRequest register = new RegisterRequest();
        register.setName("Test Login");
        register.setEmail("login@example.com");
        register.setPassword("mypassword");
        register.setPhoneNumber("555-0000");
        register.setRole(Role.LIBRARIAN);

        String encodedPassword = passwordEncoder.encode(register.getPassword());

        User user = new User();
        user.setName(register.getName());
        user.setEmail(register.getEmail());
        user.setPassword(encodedPassword);
        user.setPhoneNumber(register.getPhoneNumber());
        user.setRole(register.getRole());

        userRepository.save(user);

        AuthenticationRequest loginRequest = new AuthenticationRequest();
        loginRequest.setEmail("login@example.com");
        loginRequest.setPassword("mypassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }


    @Test
    void login_WithInvalidCredentials_ShouldReturn403() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        request.setEmail("invalid@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Bad credentials"));

    }

    @Test
    void register_WithExistingEmail_ShouldReturnConflict() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setName("First User");
        request.setEmail("duplicate@example.com");
        request.setPassword("password");
        request.setPhoneNumber("1234567890");
        request.setRole(Role.LIBRARIAN);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

}
