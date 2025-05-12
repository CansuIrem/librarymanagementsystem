package com.cansuiremkanli.libmanage.publisher;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import com.cansuiremkanli.libmanage.data.dto.BookDTO;

@Slf4j
@Component
public class BookStockPublisher {

    private final Sinks.Many<BookDTO> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(BookDTO bookDTO) {
        var result = sink.tryEmitNext(bookDTO);
        if (result.isSuccess()) {
            log.debug("Published book stock update for book ID: {}", bookDTO.getId());
        } else {
            log.warn("Failed to publish book stock update for book ID: {}. EmitResult: {}", bookDTO.getId(), result);
        }
    }

    public Flux<BookDTO> getBookStream() {
        log.info("Reactive book stock stream subscribed");
        return sink.asFlux()
                .doOnSubscribe(subscription -> log.debug("New subscriber to book stock stream"))
                .doOnCancel(() -> log.debug("A subscriber has disconnected from book stock stream"));
    }
}
