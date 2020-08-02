package com.artarkatesoft.webfluxstockquoteservice.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

//@Component
@Slf4j
@RequiredArgsConstructor
public class QuoteRunner implements CommandLineRunner {

    private final StockQuoteClient stockQuoteClient;

    @Override
    public void run(String... args) throws Exception {
        stockQuoteClient.getQuoteStream()
                .subscribe(quote -> log.info("Received quote is {}", quote));
    }
}
