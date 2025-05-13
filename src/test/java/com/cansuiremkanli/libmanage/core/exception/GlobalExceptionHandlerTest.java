package com.cansuiremkanli.libmanage.core.exception;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleEntityNotFound_shouldReturn404() {
        EntityNotFoundException ex = new EntityNotFoundException("User not found");
        ResponseEntity<Map<String, String>> response = handler.handleEntityNotFound(ex);
        assertEquals(404, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody().get("error"));
    }

    @Test
    void handleIllegalArgument_shouldReturn400() {
        IllegalArgumentException ex = new IllegalArgumentException("Invalid input");
        ResponseEntity<Map<String, String>> response = handler.handleIllegalArgument(ex);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Invalid input", response.getBody().get("error"));
    }

    @Test
    void handleAccessDenied_shouldReturn403() {
        AccessDeniedException ex = new AccessDeniedException("Forbidden access");
        ResponseEntity<Map<String, String>> response = handler.handleAccessDenied(ex);
        assertEquals(403, response.getStatusCodeValue());
        assertTrue(response.getBody().get("error").contains("Access denied"));
    }

    @Test
    void handleUsernameNotFound_shouldReturn403() {
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found");
        ResponseEntity<Map<String, String>> response = handler.handleAuthExceptions(ex);
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("User not found", response.getBody().get("error"));
    }

    @Test
    void handleBadCredentials_shouldReturn403() {
        BadCredentialsException ex = new BadCredentialsException("Wrong password");
        ResponseEntity<Map<String, String>> response = handler.handleAuthExceptions(ex);
        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Wrong password", response.getBody().get("error"));
    }

    @Test
    void handleIllegalState_shouldReturn409() {
        IllegalStateException ex = new IllegalStateException("Conflict occurred");
        ResponseEntity<Map<String, String>> response = handler.handleIllegalState(ex);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Conflict occurred", response.getBody().get("error"));
    }

    @Test
    void handleDataIntegrityViolation_shouldReturn409() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Email exists");
        ResponseEntity<Map<String, String>> response = handler.handleConstraintViolation(ex);
        assertEquals(409, response.getStatusCodeValue());
        assertEquals("Email already exists", response.getBody().get("error"));
    }

    @Test
    void handleGeneral_shouldReturn500() {
        Exception ex = new Exception("Something failed");
        ResponseEntity<String> response = handler.handleGeneral(ex);

        assertEquals(500, response.getStatusCodeValue());
        assertTrue(response.getBody().contains("Something failed"));
        assertTrue(response.getBody().startsWith("Internal error:"));
    }


    @Test
    void handleAuthorizationDenied_shouldReturn403() {
        AuthorizationDecision decision = new AuthorizationDecision(false);
        AuthorizationDeniedException ex = new AuthorizationDeniedException("No permission", decision);

        ResponseEntity<Map<String, String>> response = handler.handleAccessDenied(ex);

        assertEquals(403, response.getStatusCodeValue());
        assertEquals("Access denied. No permission", response.getBody().get("error"));
    }


    @Test
    void handleValidationErrors_shouldReturn400() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        FieldError fieldError = new FieldError("userDTO", "email", "Email format is invalid");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<Map<String, String>> response = handler.handleValidationErrors(ex);
        assertEquals(400, response.getStatusCodeValue());
        assertEquals("Email format is invalid", response.getBody().get("email"));
    }
}
