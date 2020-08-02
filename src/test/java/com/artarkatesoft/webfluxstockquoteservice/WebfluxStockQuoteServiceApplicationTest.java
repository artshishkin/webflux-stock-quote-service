package com.artarkatesoft.webfluxstockquoteservice;

import com.artarkatesoft.webfluxstockquoteservice.model.Quote;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.concurrent.CountDownLatch;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class WebfluxStockQuoteServiceApplicationTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void testFetchQuotes() {
        webTestClient
                .get()
                .uri("/quotes?size=20")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(Quote.class)
                .hasSize(20)
                .consumeWith(allQuotes -> {
                    assertThat(allQuotes.getResponseBody()).allSatisfy(quote -> assertThat(quote.getPrice()).isPositive());
                    assertThat(allQuotes.getResponseBody()).hasSize(20);
                });
    }

    @Test
    void testStreamQuotes_countDownLatch() throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(10);
        webTestClient
                .get()
                .uri("/quotes")
                .accept(MediaType.APPLICATION_STREAM_JSON)
                .exchange()
                .returnResult(Quote.class)
                .getResponseBody()
                .take(10)
                .subscribe(quote -> {
                    assertThat(quote.getPrice()).isPositive();
                    countDownLatch.countDown();
                });
        countDownLatch.await();
        System.out.println("Test complete");
    }

    @Test
    void testStreamQuotes_stepVerifier() {
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
    }
}
