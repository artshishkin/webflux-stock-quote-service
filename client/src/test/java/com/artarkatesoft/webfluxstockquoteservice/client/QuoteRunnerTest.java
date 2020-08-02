package com.artarkatesoft.webfluxstockquoteservice.client;

import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import com.artarkatesoft.webfluxstockquoteservice.client.repository.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import java.time.Instant;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QuoteRunnerTest {

    @InjectMocks
    QuoteRunner quoteRunner;

    @Mock
    QuoteRepository quoteRepository;

    @Test
    void run() throws Exception {
        //given
        Flux<Quote> defaultQuoteFlux = Flux.range(1, 20).map(this::createFakeQuote);
        given(quoteRepository.findWithTailableCursorBy()).willReturn(defaultQuoteFlux);
        //when
        quoteRunner.run();
        //then
        then(quoteRepository).should().findWithTailableCursorBy();
    }

    private Quote createFakeQuote(int i) {
        Quote quote = new Quote("Ticker" + i, 100.0 + i);
        quote.setId("ID" + i);
        quote.setInstant(Instant.now());
        return quote;
    }
}