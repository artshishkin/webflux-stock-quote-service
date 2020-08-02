package com.artarkatesoft.webfluxstockquoteservice.service;

import com.artarkatesoft.webfluxstockquoteservice.model.Quote;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

class QuoteGeneratorServiceImplTest {

    QuoteGeneratorServiceImpl quoteGeneratorService = new QuoteGeneratorServiceImpl();

    @Test
    void fetchQuotesStream() throws InterruptedException {
        Flux<Quote> quoteFlux = quoteGeneratorService.fetchQuoteStream(Duration.ofMillis(100L));
        quoteFlux.take(10)
                .subscribe(System.out::println);
//        Thread.sleep(1000);
    }

    @Test
    void fetchQuotesStreamMy() throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(1);

        Flux<Quote> quoteFlux = quoteGeneratorService.fetchQuoteStream(Duration.ofMillis(100L));
        quoteFlux
                .take(10)
                .subscribe(System.out::println, null, latch::countDown);

        latch.await();
    }

    @Test
    void fetchQuotesStream_count() {
        //when
        Flux<Quote> quoteFlux = quoteGeneratorService.fetchQuoteStream(Duration.ofMillis(100L));
        //then
        StepVerifier.create(quoteFlux.take(10).log("fetchQuotesStream_take"))
                .expectSubscription()
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    void fetchQuotesStream_recorded() {
        //when
        List<Quote> quotesRecorder = new ArrayList<>();
        Flux<Quote> quoteFlux = quoteGeneratorService.fetchQuoteStream(Duration.ofMillis(100L));
        //then
        StepVerifier.create(quoteFlux.take(10).log("fetchQuotesStream_take"))
                .expectSubscription()
                .recordWith(() -> quotesRecorder)
                .thenConsumeWhile(quote -> true)
                .consumeRecordedWith(quotes ->
                        assertThat(quotes).hasSize(10).allSatisfy(quote -> assertThat(quote.getPrice()).isPositive()))
                .verifyComplete();
    }
}
