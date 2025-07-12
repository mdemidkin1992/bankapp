package ru.mdemidkin.convert.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import ru.mdemidkin.convert.client.ExchangeClient;
import ru.mdemidkin.libdto.account.CurrencyDto;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConvertServiceTest {

    @Mock
    private ExchangeClient exchangeClient;

    @InjectMocks
    private ConvertService convertService;

    @Test
    void convertAmount_calculatesCorrectConversion() {
        List<CurrencyDto> currencies = List.of(
                new CurrencyDto("USD", "USD", new BigDecimal("1.2")),
                new CurrencyDto("EUR", "EUR", new BigDecimal("0.8"))
        );
        when(exchangeClient.getCurrencies()).thenReturn(Flux.fromIterable(currencies));
        BigDecimal result = convertService
                .convertAmount("USD", "EUR", new BigDecimal("100"))
                .block();
        assertEquals(new BigDecimal("150.0"), result);
    }

    @Test
    void convertAmount_appliesHalfUpRounding() {
        List<CurrencyDto> currencies = List.of(
                new CurrencyDto("AAA", "AAA", new BigDecimal("1")),
                new CurrencyDto("BBB", "BBB", new BigDecimal("3"))
        );
        when(exchangeClient.getCurrencies()).thenReturn(Flux.fromIterable(currencies));
        BigDecimal result = convertService
                .convertAmount("AAA", "BBB", new BigDecimal("100"))
                .block();
        assertEquals(new BigDecimal("33"), result);
    }
}
