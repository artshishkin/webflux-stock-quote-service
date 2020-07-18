package com.artarkatesoft.webfluxstockquoteservice.service;

import com.artarkatesoft.webfluxstockquoteservice.model.Quote;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

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
}
