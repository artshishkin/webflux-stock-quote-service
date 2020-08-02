package com.artarkatesoft.webfluxstockquoteservice.client;

import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import com.artarkatesoft.webfluxstockquoteservice.client.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Component
@Slf4j
@RequiredArgsConstructor
public class QuoteRunner implements CommandLineRunner {

    private final QuoteRepository quoteRepository;

    @Override
    public void run(String... args) throws Exception {
        Flux<Quote> tailableCursorQuoteFlux = quoteRepository.findWithTailableCursorBy();
        tailableCursorQuoteFlux.subscribe(quote -> log.debug("Quote from DB: {}", quote));
    }
}
