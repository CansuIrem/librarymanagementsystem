package com.cansuiremkanli.libmanage.publisher;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import com.cansuiremkanli.libmanage.data.dto.BookDTO;

@Component
public class BookStockPublisher {

    private final Sinks.Many<BookDTO> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(BookDTO bookDTO) {
        sink.tryEmitNext(bookDTO);
    }

    public Flux<BookDTO> getBookStream() {
        return sink.asFlux();
    }
}
