package ru.mdemidkin.notifications.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mdemidkin.libdto.notification.NotificationDto;
import ru.mdemidkin.notifications.model.Notification;
import ru.mdemidkin.notifications.service.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsListenerService {

    private final NotificationService service;

    @KafkaListener(
            topics = "${kafka-topics.notifications-topic}",
            groupId = "${spring.application.name}.notifications-consumer-group")
    public Mono<Notification> listenNotifications(@Payload NotificationDto notification) {
        log.info("Получено сообщение {}", notification);
        return service.notify(notification.getLogin(), notification.getMessage());
    }
}
