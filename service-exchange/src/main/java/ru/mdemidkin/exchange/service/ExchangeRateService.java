package ru.mdemidkin.exchange.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.exchange.model.Currency;
import ru.mdemidkin.exchange.repository.CurrencyRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ExchangeRateService {

    private final CurrencyRepository currencyRepository;

    public Flux<Currency> getAllCurrentRates() {
        return currencyRepository.findAllDistinctTitles()
                .filter(title -> !"RUB".equalsIgnoreCase(title))
                .flatMap(currencyRepository::findRandomByTitle)
                .concatWith(Mono.just(createRubCurrency()));
    }

    private Currency createRubCurrency() {
        Currency rub = new Currency();
        rub.setTitle("RUB");
        rub.setName("Рубль");
        rub.setValue(BigDecimal.ONE);
        return rub;
    }
}
