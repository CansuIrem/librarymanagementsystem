package com.cansuiremkanli.libmanage.publisher;


import com.cansuiremkanli.libmanage.data.dto.BookDTO;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

class BookStockPublisherTest {

    @Test
    void testPublishAndReceiveBook() {

        BookStockPublisher publisher = new BookStockPublisher();


        BookDTO book = new BookDTO();
        book.setTitle("Reactive Java");
        book.setAuthor("İrem Kanlı");
        book.setAvailableCount(7);

        Flux<BookDTO> bookStream = publisher.getBookStream().take(1);

        StepVerifier.create(bookStream)
                .then(() -> publisher.publish(book))
                .expectNextMatches(b ->
                        "Reactive Java".equals(b.getTitle()) &&
                                "İrem Kanlı".equals(b.getAuthor()) &&
                                b.getAvailableCount() == 7
                )
                .verifyComplete();
    }
}
