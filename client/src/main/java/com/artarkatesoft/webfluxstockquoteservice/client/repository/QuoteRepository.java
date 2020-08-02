package com.artarkatesoft.webfluxstockquoteservice.client.repository;

import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface QuoteRepository extends ReactiveMongoRepository<Quote, String> {
}
