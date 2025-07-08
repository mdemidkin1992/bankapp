package ru.mdemidkin.cash.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import ru.mdemidkin.cash.model.Notification;

public interface NotificationRepository extends R2dbcRepository<Notification, Long> {
}
