package com.cansuiremkanli.libmanage.controller;

import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import com.cansuiremkanli.libmanage.publisher.BookStockPublisher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

@WebFluxTest(ReactiveBookController.class)
@Import(BookStockPublisher.class)
public class ReactiveBookControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private BookStockPublisher bookStockPublisher;

    @WithMockUser(roles = "LIBRARIAN")
    @Test
    void testBookStreamReturnsPublishedBook() {
        // Hazırlık: test BookDTO nesnesi oluştur
        BookDTO bookDTO = new BookDTO();
        bookDTO.setTitle("Reactive Programming");
        bookDTO.setAuthor("İrem Kanli");
        bookDTO.setAvailableCount(5);

        // Yayına başla
        bookStockPublisher.publish(bookDTO);

        // WebTestClient ile endpoint'e bağlan ve veriyi doğrula
        webTestClient.get()
                .uri("/api/reactive/books/stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(BookDTO.class)
                .getResponseBody()
                .as(StepVerifier::create)
                .expectNextMatches(book ->
                        book.getTitle().equals("Reactive Programming") &&
                                book.getAuthor().equals("İrem Kanli") &&
                                book.getAvailableCount() == 5
                )
                .thenCancel()
                .verify();
    }
}
