
package com.cansuiremkanli.libmanage.service.impl;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.core.security.JwtService;
import com.cansuiremkanli.libmanage.data.dto.AuthenticationRequest;
import com.cansuiremkanli.libmanage.data.dto.AuthenticationResponse;
import com.cansuiremkanli.libmanage.data.dto.RegisterRequest;
import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @InjectMocks
    private AuthServiceImpl authService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("pass");
        registerRequest.setName("Test User");
        registerRequest.setPhoneNumber("1234567890");
        registerRequest.setRole(Role.PATRON);

        authRequest = new AuthenticationRequest();
        authRequest.setEmail("test@example.com");
        authRequest.setPassword("pass");

        user = new User();
        user.setEmail("test@example.com");
        user.setPassword("encoded-pass");
        user.setRole(Role.PATRON);
    }

    @Test
    void testRegister() {
        when(passwordEncoder.encode("pass")).thenReturn("encoded-pass");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtService.generateToken(any(User.class))).thenReturn("mock-jwt");

        AuthenticationResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("mock-jwt", response.getToken());
    }

    @Test
    void testAuthenticate() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("mock-jwt");

        AuthenticationResponse response = authService.authenticate(authRequest);

        assertNotNull(response);
        assertEquals("mock-jwt", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
