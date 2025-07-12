package ru.mdemidkin.exchange.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.exchange.model.Currency;
import ru.mdemidkin.exchange.repository.CurrencyRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void getAllCurrentRates_filtersOutRubAndAddsRubLast() {
        given(currencyRepository.findAllDistinctTitles())
                .willReturn(Flux.just("USD", "rub", "EUR"));
        given(currencyRepository.findRandomByTitle("USD"))
                .willReturn(Mono.just(createCurrency("Dollar", "USD", "1.0")));
        given(currencyRepository.findRandomByTitle("EUR"))
                .willReturn(Mono.just(createCurrency("Euro", "EUR", "0.9")));

        List<Currency> result = exchangeRateService.getAllCurrentRates()
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(3, result.size());

        Currency first = result.get(0);
        assertEquals("USD", first.getTitle());
        assertEquals("Dollar", first.getName());
        assertEquals(0, first.getValue().compareTo(new BigDecimal("1.0")));

        Currency second = result.get(1);
        assertEquals("EUR", second.getTitle());
        assertEquals("Euro", second.getName());
        assertEquals(0, second.getValue().compareTo(new BigDecimal("0.9")));

        Currency rub = result.get(2);
        assertEquals("RUB", rub.getTitle());
        assertEquals("Рубль", rub.getName());
        assertEquals(0, rub.getValue().compareTo(BigDecimal.ONE));
    }

    @Test
    void getAllCurrentRates_emptyRepositoryStillReturnsRub() {
        given(currencyRepository.findAllDistinctTitles())
                .willReturn(Flux.empty());

        List<Currency> result = exchangeRateService.getAllCurrentRates()
                .collectList()
                .block();

        assertNotNull(result);
        assertEquals(1, result.size());
        Currency rub = result.get(0);
        assertEquals("RUB", rub.getTitle());
        assertEquals("Рубль", rub.getName());
        assertEquals(0, rub.getValue().compareTo(BigDecimal.ONE));
    }

    private Currency createCurrency(String name, String title, String value) {
        Currency c = new Currency();
        c.setName(name);
        c.setTitle(title);
        c.setValue(new BigDecimal(value));
        return c;
    }
}
