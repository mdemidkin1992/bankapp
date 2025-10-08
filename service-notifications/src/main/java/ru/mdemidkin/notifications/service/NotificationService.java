package ru.mdemidkin.notifications.service;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mdemidkin.notifications.model.Notification;
import ru.mdemidkin.notifications.repository.NotificationRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final MeterRegistry meterRegistry;

    public Mono<Notification> notify(String login, String message) {
        try {
            Notification notification = Notification.builder()
                    .login(login)
                    .message(message)
                    .time(LocalDateTime.now())
                    .build();

            return notificationRepository.save(notification);
        } catch (Exception e) {
            meterRegistry.counter("notifications_error", "login", login).increment();
            return Mono.error(e);
        }
    }

    public Flux<Notification> getUserNotifications(String login) {
        return notificationRepository.findAllByLogin(login);
    }
}
