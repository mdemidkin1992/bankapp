package ru.mdemidkin.convert.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mdemidkin.convert.listener.ExchangeRateListener;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConvertService {

    private final ExchangeRateListener listener;

    public Mono<BigDecimal> convertAmount(String fromCurrency,
                                          String toCurrency,
                                          BigDecimal value) {
        return Mono.fromSupplier(() -> {
            BigDecimal fromRate = getCurrencyRate(fromCurrency);
            BigDecimal toRate = getCurrencyRate(toCurrency);
            return value.multiply(fromRate).divide(toRate, RoundingMode.HALF_UP);
        });
    }

    private BigDecimal getCurrencyRate(String currency) {
        return listener.getRate(currency)
                .orElseThrow(() -> new IllegalArgumentException("Курс валюты " + currency + " не найден"));
    }
}
