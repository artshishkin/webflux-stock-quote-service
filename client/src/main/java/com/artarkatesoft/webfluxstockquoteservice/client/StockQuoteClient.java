package com.artarkatesoft.webfluxstockquoteservice.client;

import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;

import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON;

@Component
@ConfigurationProperties(prefix = "app.client")
@RequiredArgsConstructor
@Setter
@Slf4j
public class StockQuoteClient {
    private String host;
    private String port;
    private String path;

    @Setter(AccessLevel.PRIVATE)
    private WebClient webClient;
    @Setter(AccessLevel.PRIVATE)
    private String requestUrl;

    @PostConstruct
    private void init() {
        String baseUrl = "http://" + host + ":" + port;
        webClient = WebClient.builder().baseUrl(baseUrl).build();
        requestUrl = baseUrl + path;
    }

    public Flux<Quote> getQuoteStream() {
        log.debug("client request url is " + requestUrl);
        return webClient.get().uri(path)
                .accept(APPLICATION_STREAM_JSON)
                .retrieve()
                .bodyToFlux(Quote.class)
                .retry().log("StockQuoteClient: getQuoteStream");
    }
}
