package ru.mdemidkin.exchange.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.mdemidkin.exchange.model.Currency;
import ru.mdemidkin.exchange.service.ExchangeRateService;
import ru.mdemidkin.libdto.account.CurrencyDto;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ExchangeRatesController {

    private final ExchangeRateService exchangeRateService;

    @GetMapping("/api/rates")
    public Flux<CurrencyDto> getUserByUsername() {
        return exchangeRateService.getAllCurrentRates()
                .map(this::mapToDto);
    }

    private CurrencyDto mapToDto(Currency currency) {
        return CurrencyDto.builder()
                .name(currency.getName())
                .title(currency.getTitle())
                .value(currency.getValue())
                .build();
    }

}
