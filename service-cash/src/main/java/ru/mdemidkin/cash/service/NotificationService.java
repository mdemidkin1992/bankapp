package ru.mdemidkin.cash.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mdemidkin.cash.model.Notification;
import ru.mdemidkin.cash.repository.NotificationRepository;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
// todo перенести в service-notification
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

}
