
package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.AuthenticationRequest;
import com.cansuiremkanli.libmanage.data.dto.AuthenticationResponse;
import com.cansuiremkanli.libmanage.data.dto.RegisterRequest;
import com.cansuiremkanli.libmanage.service.AuthService;
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
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest{

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    private ObjectMapper objectMapper;
    private AuthenticationResponse authResponse;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        authResponse = new AuthenticationResponse();
        authResponse.setToken("mock-jwt-token");
    }

    @Test
    void testRegister() throws Exception {
        RegisterRequest request = new RegisterRequest();
        Mockito.when(authService.register(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testLogin() throws Exception {
        AuthenticationRequest request = new AuthenticationRequest();
        Mockito.when(authService.authenticate(any())).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
