package ru.mdemidkin.notifications.service;

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

    public Mono<Notification> notify(String login, String message) {
        Notification notification = Notification.builder()
                .login(login)
                .message(message)
                .time(LocalDateTime.now())
                .build();

        return notificationRepository.save(notification);
    }

    public Flux<Notification> getUserNotifications(String login) {
        return notificationRepository.findAllByLogin(login);
    }
}
