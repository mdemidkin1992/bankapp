package ru.mdemidkin.exchange.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.mdemidkin.exchange.model.Currency;
import ru.mdemidkin.exchange.service.ExchangeRateService;
import ru.mdemidkin.libdto.account.CurrencyDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ExchangeRateService exchangeRateService;

    @Value("${kafka-topics.exchange-rates-topic}")
    private String exchangeRateTopic;

    @Scheduled(fixedRate = 1000)
    public void publishCurrentRates() {
        exchangeRateService.getAllCurrentRates()
                .subscribe(rate -> {
                    ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(exchangeRateTopic, rate.getTitle(), mapToDto(rate));
                    kafkaTemplate.send(producerRecord);
                    log.info("Отправлено сообщение {} в топик {}", producerRecord.value(), exchangeRateTopic);
                });
    }

    private CurrencyDto mapToDto(Currency currency) {
        return CurrencyDto.builder()
                .name(currency.getName())
                .title(currency.getTitle())
                .value(currency.getValue())
                .build();
    }
}
