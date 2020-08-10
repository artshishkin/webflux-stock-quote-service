package com.artarkatesoft.webfluxstockquoteservice.client;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.artarkatesoft.webfluxstockquoteservice.client.model.Quote;
import com.artarkatesoft.webfluxstockquoteservice.client.repository.QuoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.atLeast;

@ExtendWith(MockitoExtension.class)
class QuoteRunnerTest {

    @InjectMocks
    QuoteRunner quoteRunner;

    @Mock
    QuoteRepository quoteRepository;

    @Mock
    private Appender mockedAppender;

    @Captor
    private ArgumentCaptor<LoggingEvent> loggingEventCaptor;

    @BeforeEach
    void setUp() {
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.addAppender(mockedAppender);
        root.setLevel(Level.DEBUG);
    }

    @Test
    void run() throws Exception {
        //given
        List<Quote> defaultQuotes = IntStream.rangeClosed(1, 20)
                .mapToObj(this::createFakeQuote)
                .collect(Collectors.toList());
        Flux<Quote> defaultQuoteFlux = Flux.fromIterable(defaultQuotes);
        given(quoteRepository.findWithTailableCursorBy()).willReturn(defaultQuoteFlux);
        //when
        quoteRunner.run();
        //then
        then(quoteRepository).should().findWithTailableCursorBy();
        then(mockedAppender).should(atLeast(20)).doAppend(loggingEventCaptor.capture());
        List<LoggingEvent> loggingEvents = loggingEventCaptor
                .getAllValues()
                .stream()
                .filter(loggingEvent -> loggingEvent.getLoggerName().equals("com.artarkatesoft.webfluxstockquoteservice.client.QuoteRunner"))
                .collect(Collectors.toList());
        assertThat(loggingEvents).hasSize(20);
        loggingEvents
                .stream()
                .map(loggingEvent -> (Quote) loggingEvent.getArgumentArray()[0])
                .forEach(quote -> assertThat(quote).isIn(defaultQuotes));
    }

    private Quote createFakeQuote(int i) {
        Quote quote = new Quote("Ticker" + i, 100.0 + i);
        quote.setId("ID" + i);
        quote.setInstant(Instant.now());
        return quote;
    }
}
