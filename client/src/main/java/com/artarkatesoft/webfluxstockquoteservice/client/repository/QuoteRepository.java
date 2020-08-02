package com.artarkatesoft.webfluxstockquoteservice.client.repository;

import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;

public interface QuoteRepository extends ReactiveMongoRepository<Quote, String> {
    @Tailable
    Flux<Quote> findWithTailableCursorBy();
}
