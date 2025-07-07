package ru.mdemidkin.gateway.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;
import ru.mdemidkin.gateway.model.User;

public interface UserRepository extends R2dbcRepository<User, Long> {
    Mono<User> findByLogin(String login);
}
