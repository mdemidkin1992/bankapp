package ru.mdemidkin.cash.producer;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import ru.mdemidkin.libdto.notification.NotificationDto;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@SpringBootTest(classes = NotificationsProducerService.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "kafka-topics.notifications-topic=topic-bankapp-notifications")
class NotificationsProducerServiceTest {

    @Autowired
    private NotificationsProducerService notificationsProducerService;

    @MockBean
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Test
    void sendNotificationsMessage_shouldSendMessageToKafka() {
        // Arrange
        String testKey = "user123";
        NotificationDto testNotification = new NotificationDto("user123", "Test notification message");

        // Act
        notificationsProducerService.sendNotificationsMessage(testKey, testNotification);

        // Assert
        verify(kafkaTemplate, times(1)).send(any(ProducerRecord.class));
    }

    @Test
    void sendNotificationsMessage_shouldSendMultipleMessages() {
        // Arrange
        NotificationDto notification1 = new NotificationDto("user1", "Message 1");
        NotificationDto notification2 = new NotificationDto("user2", "Message 2");

        // Act
        notificationsProducerService.sendNotificationsMessage("key1", notification1);
        notificationsProducerService.sendNotificationsMessage("key2", notification2);

        // Assert
        verify(kafkaTemplate, times(2)).send(any(ProducerRecord.class));
    }

    @Test
    void sendNotificationsMessage_withSameKey_shouldSendToSamePartition() {
        // Arrange
        String sameKey = "user123";
        NotificationDto notification1 = new NotificationDto("user123", "First message");
        NotificationDto notification2 = new NotificationDto("user123", "Second message");

        // Act
        notificationsProducerService.sendNotificationsMessage(sameKey, notification1);
        notificationsProducerService.sendNotificationsMessage(sameKey, notification2);

        // Assert
        verify(kafkaTemplate, times(2)).send(any(ProducerRecord.class));
    }
}