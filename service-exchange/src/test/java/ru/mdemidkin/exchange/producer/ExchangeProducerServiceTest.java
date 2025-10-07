package ru.mdemidkin.exchange.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import reactor.core.publisher.Flux;
import ru.mdemidkin.exchange.model.Currency;
import ru.mdemidkin.exchange.service.ExchangeRateService;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = ExchangeProducerService.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "kafka-topics.exchange-rates-topic=topic-bankapp-exchange-rates")
class ExchangeProducerServiceTest {

    @Autowired
    private ExchangeProducerService exchangeProducerService;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private ExchangeRateService exchangeRateService;

    @Test
    void publishCurrentRates_shouldSendMessageToKafka() {
        // Arrange
        Currency testCurrency = Currency.builder()
                .title("USD")
                .name("US Dollar")
                .value(new BigDecimal("74.50"))
                .build();

        when(exchangeRateService.getAllCurrentRates())
                .thenReturn(Flux.just(testCurrency));

        // Act
        exchangeProducerService.publishCurrentRates();

        // Assert
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void publishCurrentRates_shouldSendMultipleMessages() {
        // Arrange
        Currency usd = Currency.builder()
                .title("USD")
                .name("US Dollar")
                .value(new BigDecimal("74.50"))
                .build();

        Currency eur = Currency.builder()
                .title("EUR")
                .name("Euro")
                .value(new BigDecimal("82.30"))
                .build();

        when(exchangeRateService.getAllCurrentRates())
                .thenReturn(Flux.just(usd, eur));

        // Act
        exchangeProducerService.publishCurrentRates();

        // Assert
        verify(kafkaTemplate, times(2)).send(any(ProducerRecord.class));
    }

    @Test
    void publishCurrentRates_withSameCurrency_shouldSendToSamePartition() {
        // Arrange
        Currency usd1 = Currency.builder()
                .title("USD")
                .name("US Dollar")
                .value(new BigDecimal("74.50"))
                .build();

        Currency usd2 = Currency.builder()
                .title("USD")
                .name("US Dollar")
                .value(new BigDecimal("74.60"))
                .build();

        when(exchangeRateService.getAllCurrentRates())
                .thenReturn(Flux.just(usd1, usd2));

        // Act
        exchangeProducerService.publishCurrentRates();

        // Assert
        verify(kafkaTemplate, times(2)).send(any(ProducerRecord.class));
    }
}