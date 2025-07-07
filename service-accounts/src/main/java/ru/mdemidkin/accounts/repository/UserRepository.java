package ru.mdemidkin.accounts.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import ru.mdemidkin.accounts.model.User;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByLogin(String login);
}
