package ru.mdemidkin.cash.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.mdemidkin.libdto.notification.NotificationDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka-topics.notifications-topic}")
    private String notificationsTopic;

    public void sendNotificationsMessage(String key, NotificationDto message) {
        ProducerRecord<String, Object> producerRecord = new ProducerRecord<>(notificationsTopic, key, message);
        kafkaTemplate.send(producerRecord);
        log.info("Отправлено сообщение {} в топик {}", message, notificationsTopic);
    }
}
