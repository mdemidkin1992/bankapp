package ru.mdemidkin.convert.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.mdemidkin.convert.listener.ExchangeRateListener;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConvertServiceTest {

    @Mock
    private ExchangeRateListener listener;

    @InjectMocks
    private ConvertService convertService;

    @Test
    void convertAmount_calculatesCorrectConversion() {
        when(listener.getRate("USD")).thenReturn(Optional.of(new BigDecimal("1.2")));
        when(listener.getRate("EUR")).thenReturn(Optional.of(new BigDecimal("0.8")));
        
        BigDecimal result = convertService
                .convertAmount("USD", "EUR", new BigDecimal("100"))
                .block();
        assertEquals(0, new BigDecimal("150.0").compareTo(result));
    }

    @Test
    void convertAmount_appliesHalfUpRounding() {
        when(listener.getRate("AAA")).thenReturn(Optional.of(new BigDecimal("1")));
        when(listener.getRate("BBB")).thenReturn(Optional.of(new BigDecimal("3")));
        
        BigDecimal result = convertService
                .convertAmount("AAA", "BBB", new BigDecimal("100"))
                .block();
        assertEquals(0, new BigDecimal("33").compareTo(result));
    }
}
