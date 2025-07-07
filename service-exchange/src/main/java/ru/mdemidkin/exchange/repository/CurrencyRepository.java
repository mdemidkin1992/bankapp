package ru.mdemidkin.exchange.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.exchange.model.Currency;

public interface CurrencyRepository extends R2dbcRepository<Currency, Long> {

    @Query("SELECT DISTINCT title FROM currency")
    Flux<String> findAllDistinctTitles();

    @Query("""
        SELECT * FROM currency 
        WHERE title = :title 
        ORDER BY RANDOM() 
        LIMIT 1
    """)
    Mono<Currency> findRandomByTitle(String title);
}
