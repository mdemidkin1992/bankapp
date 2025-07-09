package ru.mdemidkin.exchange.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import ru.mdemidkin.exchange.model.Notification;

public interface NotificationRepository extends R2dbcRepository<Notification, Long> {
    Flux<Notification> findAllByLogin(String login);
}
