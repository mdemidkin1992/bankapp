package ru.mdemidkin.accounts.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.model.Account;

public interface AccountRepository extends R2dbcRepository<Account, Long> {
    Flux<Account> findByUserId(Long userId);
    Mono<Void> deleteByUserIdAndCurrency(Long userId, String currency);
}
