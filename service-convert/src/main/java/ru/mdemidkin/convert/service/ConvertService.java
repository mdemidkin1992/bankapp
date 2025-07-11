package ru.mdemidkin.convert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mdemidkin.convert.client.ExchangeClient;
import ru.mdemidkin.libdto.account.CurrencyDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvertService {

    private final ExchangeClient exchangeClient;

    public Mono<BigDecimal> convertAmount(String fromCurrency,
                                          String toCurrency,
                                          BigDecimal value) {
        return exchangeClient.getCurrencies()
                .collectList()
                .flatMap(list -> {
                    Map<String, BigDecimal> currencyMap = list.stream()
                            .collect(Collectors.toMap(
                                    CurrencyDto::getTitle,
                                    CurrencyDto::getValue));

                    BigDecimal fromRate = currencyMap.get(fromCurrency);
                    BigDecimal toRate = currencyMap.get(toCurrency);

                    return Mono.just(value
                            .multiply(fromRate)
                            .divide(toRate, RoundingMode.HALF_UP));
                });
    }
}
