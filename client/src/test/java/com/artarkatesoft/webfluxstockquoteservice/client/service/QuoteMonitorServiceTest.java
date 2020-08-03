package com.artarkatesoft.webfluxstockquoteservice.client.service;

import com.artarkatesoft.webfluxstockquoteservice.client.StockQuoteClient;
import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import com.artarkatesoft.webfluxstockquoteservice.client.repository.QuoteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class QuoteMonitorServiceTest {

    @Mock
    StockQuoteClient stockQuoteClient;

    @Mock
    QuoteRepository quoteRepository;

    @InjectMocks
    QuoteMonitorService quoteMonitorService;

    @Test
    void onApplicationEvent() {
        //given
        Flux<Quote> defaultQuoteFlux = Flux.range(1, 20).map(this::createFakeQuote);
        Quote defaultQuote = createFakeQuote(21);
        given(stockQuoteClient.getQuoteStream()).willReturn(defaultQuoteFlux);
        given(quoteRepository.insert(any(Quote.class))).willReturn(Mono.just(defaultQuote));
        //when
        quoteMonitorService.onApplicationEvent(null);
        //then
        then(stockQuoteClient).should().getQuoteStream();
        then(quoteRepository).should(times(20)).insert(any(Quote.class));
    }

    private Quote createFakeQuote(int i) {
        Quote quote = new Quote("Ticker" + i, 100.0 + i);
        quote.setId("ID" + i);
        quote.setInstant(Instant.now());
        return quote;
    }
}