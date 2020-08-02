package com.artarkatesoft.webfluxstockquoteservice.client.service;

import com.artarkatesoft.webfluxstockquoteservice.client.StockQuoteClient;
import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import com.artarkatesoft.webfluxstockquoteservice.client.repository.QuoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuoteMonitorService implements ApplicationListener<ContextRefreshedEvent> {

    private final StockQuoteClient stockQuoteClient;
    private final QuoteRepository quoteRepository;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Flux<Quote> savedQuotes = stockQuoteClient.getQuoteStream()
                .flatMap(quoteRepository::insert);
        savedQuotes
                .subscribe(quote -> log.debug("Saved quote is {}", quote));
    }
}
