package com.artarkatesoft.webfluxstockquoteservice.web;

import com.artarkatesoft.webfluxstockquoteservice.model.Quote;
import com.artarkatesoft.webfluxstockquoteservice.service.QuoteGeneratorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class QuoteHandlerTest {

    @Mock
    QuoteGeneratorService quoteGeneratorService;

    @InjectMocks
    QuoteHandler quoteHandler;

    WebTestClient webTestClient;
    private Flux<Quote> defaultQuoteFlux;
    private static final int FLUX_SIZE = 20;

    @BeforeEach
    void setUp() {
        RouterFunction<ServerResponse> routerFunction = new QuoteRouter().route(quoteHandler);
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();

        defaultQuoteFlux = Flux
                .range(1, FLUX_SIZE)
                .map(i -> {
                    Quote quote = new Quote("Ticker" + i, 100.1 + i);
                    quote.setInstant(Instant.now());
                    return quote;
                });
    }

    @Test
    void fetchQuotes() {
        //given
        given(quoteGeneratorService.fetchQuoteStream(any(Duration.class)))
                .willReturn(defaultQuoteFlux);
        //when
        webTestClient
                .get()
                .uri("/quotes?size=" + FLUX_SIZE)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Quote.class)
                .hasSize(FLUX_SIZE)
                .consumeWith(allQuotes -> {
                    assertThat(allQuotes.getResponseBody()).allSatisfy(quote -> assertThat(quote.getPrice()).isPositive());
                    assertThat(allQuotes.getResponseBody()).hasSize(FLUX_SIZE);
                });
        //then
        then(quoteGeneratorService).should().fetchQuoteStream(eq(Duration.ofMillis(100)));
    }

    @Test
    void streamQuotes() {
        //given
        given(quoteGeneratorService.fetchQuoteStream(any(Duration.class)))
                .willReturn(defaultQuoteFlux);
        //when
        Flux<Quote> quoteFlux = webTestClient
                .get()
                .uri("/quotes")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .returnResult(Quote.class)
                .getResponseBody();
        //then
        StepVerifier.create(quoteFlux.take(10))
                .expectSubscription()
                .thenConsumeWhile(
                        quote -> true,
                        quote -> assertThat(quote.getPrice()).isPositive())
                .verifyComplete();
        then(quoteGeneratorService).should().fetchQuoteStream(eq(Duration.ofMillis(200)));
    }
}
