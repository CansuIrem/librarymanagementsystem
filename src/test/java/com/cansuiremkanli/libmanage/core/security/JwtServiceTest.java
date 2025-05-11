package com.cansuiremkanli.libmanage.core.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private String token;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Test secret key ve expiration ayarları
        String secretKey = "ZmFrZV9zZWNyZXRfa2V5X3Rlc3RfdXNlXzMyYnl0ZVN0cmluZw=="; // Base64 encoded "fake_secret_key_test_use_32byteString"
        long expiration = 3600000; // 1 saat (ms)

        ReflectionTestUtils.setField(jwtService, "secretKey", secretKey);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", expiration);

        userDetails = new User("testuser", "password", Collections.emptyList());
        token = jwtService.generateToken(userDetails);
    }

    @Test
    void testGenerateTokenAndExtractUsername() {
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals(userDetails.getUsername(), extractedUsername);
    }

    @Test
    void testIsTokenValid_shouldReturnTrue() {
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testIsTokenExpired_shouldReturnFalse() {
        boolean result = ReflectionTestUtils.invokeMethod(jwtService, "isTokenExpired", token);
        assertFalse(result);
    }


    @Test
    void testGetExpirationTime() {
        assertEquals(3600000, jwtService.getExpirationTime());
    }
}
