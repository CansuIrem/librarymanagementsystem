package com.cansuiremkanli.libmanage.integration;

import com.cansuiremkanli.libmanage.core.enums.Role;
import com.cansuiremkanli.libmanage.data.entity.User;
import com.cansuiremkanli.libmanage.data.repository.UserRepository;
import com.cansuiremkanli.libmanage.util.JwtTestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ReactiveBookControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private JwtTestUtil jwtTestUtil;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        if (!userRepository.existsByEmail("librarian@example.com")) {
            User user = new User();
            user.setId(UUID.randomUUID());
            user.setEmail("librarian@example.com");
            user.setPassword("dummy"); // parola doğrulanmıyor çünkü JWT ile auth
            user.setName("Test Librarian");
            user.setRole(Role.LIBRARIAN);
            userRepository.save(user);
        }
    }

    @Test
    void streamBookUpdates_WithoutToken_ShouldReturnUnauthorized() {
        webTestClient.get()
                .uri("/api/reactive/books/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    void streamBookUpdates_WithInvalidRole_ShouldReturnForbidden() {
        String token = jwtTestUtil.generateToken("user@example.com", "ADMIN"); // ADMIN yetkili değil

        webTestClient.get()
                .uri("/api/reactive/books/stream")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_EVENT_STREAM_VALUE)
                .exchange()
                .expectStatus().isForbidden();
    }
}
