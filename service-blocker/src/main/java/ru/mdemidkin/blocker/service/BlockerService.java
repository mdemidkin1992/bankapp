package ru.mdemidkin.blocker.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class BlockerService {

    /**
     * Блокирует операции по времени (после 21-00 и до 6-00)
     *
     * @param now текущее время
     * @return boolean
     */
    public Mono<Boolean> isBlocked(LocalDateTime now) {
        int hour = now.getHour();
        return Mono.just(hour >= 21 || hour <= 6);
    }
}
