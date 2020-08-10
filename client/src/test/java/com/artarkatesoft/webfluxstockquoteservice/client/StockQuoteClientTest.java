package com.artarkatesoft.webfluxstockquoteservice.client;

import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.MathContext;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.MediaType.APPLICATION_STREAM_JSON_VALUE;

@ExtendWith(MockitoExtension.class)
class StockQuoteClientTest {

    public static MockWebServer mockBackEnd;
    private static final MathContext MATH_CONTEXT = new MathContext(2);

    @InjectMocks
    StockQuoteClient stockQuoteClient;

    private List<Quote> defaultQuotes;
    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @BeforeEach
    void initialize() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        String baseUrl = String.format("http://localhost:%s",
                mockBackEnd.getPort());
        stockQuoteClient.setHost("localhost");
        stockQuoteClient.setPort(mockBackEnd.getPort() + "");
        stockQuoteClient.setPath("/quotes");

//        MockitoAnnotations.initMocks(this);

        Method postConstruct = StockQuoteClient.class.getDeclaredMethod("init", null); // methodName,parameters
        postConstruct.setAccessible(true);
        postConstruct.invoke(stockQuoteClient);

        defaultQuotes = IntStream.rangeClosed(1, 20).mapToObj(this::createFakeQuote).collect(Collectors.toList());

        Jackson2JsonEncoder jackson2JsonEncoder = new Jackson2JsonEncoder();

        objectMapper = jackson2JsonEncoder.getObjectMapper();
    }

    private Quote createFakeQuote(int i) {
        Quote quote = new Quote("Ticker" + i, 100.0 + i);
        quote.setId("ID" + i);
        quote.setInstant(Instant.now());
        return quote;
    }

    @Test
    void getQuoteStream_count() throws JsonProcessingException, InterruptedException {
        //given
        String valueAsString = objectMapper.writeValueAsString(defaultQuotes);
        System.out.println("----------------" + valueAsString);

        mockBackEnd.enqueue(new MockResponse()
                .setBody(valueAsString)
                .addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_STREAM_JSON_VALUE));
        //when
        Flux<Quote> quoteFlux = stockQuoteClient.getQuoteStream();

        //then
        StepVerifier.create(quoteFlux.log("                  getQuoteStream_count-----"))
                .expectSubscription()
                .expectNextCount(10)
                .thenCancel()
                .verify();
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/quotes", recordedRequest.getPath());
    }

    @Test
    void getQuoteStream_isIn() throws JsonProcessingException, InterruptedException {
        //given
        String valueAsString = objectMapper.writeValueAsString(defaultQuotes);
        System.out.println("                ----getQuoteStream_isIn------" + valueAsString);
        mockBackEnd.enqueue(new MockResponse()
//                .setBody(buffer)
                .setBody(valueAsString)
                .addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_STREAM_JSON_VALUE));
        //when
        Flux<Quote> quoteFlux = stockQuoteClient.getQuoteStream();

        //then
        StepVerifier.create(quoteFlux.log("                  getQuoteStream_isIn-----"))
                .expectSubscription()
                .thenConsumeWhile(
                        quote -> !quote.getTicker().equals("Ticker" + 10),
                        quote -> {
                            BigDecimal oldPrice = quote.getPrice();
                            BigDecimal newPrice = new BigDecimal(String.valueOf(oldPrice), MATH_CONTEXT);
                            quote.setPrice(newPrice);
                            assertThat(quote).isIn(defaultQuotes);
                        })
                .thenCancel()
                .verify();
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/quotes", recordedRequest.getPath());
    }

    @Test
    void getQuoteStream_isIn_using_buffer() throws  InterruptedException {
        //given
        Buffer buffer = new Buffer();

        defaultQuotes.stream()
                .map(quote -> {
                    try {
                        return objectMapper.writeValueAsString(quote);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Can not map quote " + quote + " to json", e);
                    }
                })
                .forEach(stringQuote -> buffer.writeString(stringQuote, StandardCharsets.UTF_8));


        mockBackEnd.enqueue(new MockResponse()
                .setBody(buffer)
                .addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_STREAM_JSON_VALUE));
        //when
        Flux<Quote> quoteFlux = stockQuoteClient.getQuoteStream();

        //then
        StepVerifier.create(quoteFlux.log("                  getQuoteStream_isIn_using_buffer-----"))
                .expectSubscription()
                .thenConsumeWhile(
                        quote -> !quote.getTicker().equals("Ticker" + 10),
                        quote -> {
                            BigDecimal oldPrice = quote.getPrice();
                            BigDecimal newPrice = new BigDecimal(String.valueOf(oldPrice), MATH_CONTEXT);
                            quote.setPrice(newPrice);
                            assertThat(quote).isIn(defaultQuotes);
                        })
                .thenCancel()
                .verify();
        RecordedRequest recordedRequest = mockBackEnd.takeRequest();

        assertEquals("GET", recordedRequest.getMethod());
        assertEquals("/quotes", recordedRequest.getPath());
    }
}
