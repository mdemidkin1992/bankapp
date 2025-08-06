package ru.mdemidkin.notifications.listener;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import ru.mdemidkin.libdto.notification.NotificationDto;
import ru.mdemidkin.notifications.config.KafkaTestConfig;
import ru.mdemidkin.notifications.config.PostgresTestContainer;
import ru.mdemidkin.notifications.service.NotificationService;
import ru.mdemidkin.notifications.utils.KafkaHelperUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        properties = {
                "kafka-topics.notifications-topic=topic-bankapp-notifications",
                "spring.application.name=service-notifications-test",
                "spring.liquibase.enabled=false",
                "spring.security.oauth2.resourceserver.jwt.issuer-uri=",
                "spring.security.oauth2.resourceserver.jwt.jwk-set-uri="},
        classes = {NotificationsListenerService.class, KafkaTestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@EmbeddedKafka(topics = {"topic-bankapp-notifications"})
@MockBean(NotificationService.class)
public class NotificationsListenerServiceIntegrationTest extends PostgresTestContainer {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    public void testNotificationsListenerProcessesMessage() {
        String testLogin = "user123";
        String testMessage = "Test notification message";
        NotificationDto notificationDto = new NotificationDto(testLogin, testMessage);

        Map<String, Object> consumerProps = KafkaHelperUtils.createConsumerProps(embeddedKafkaBroker);

        try (var consumerForTest = new DefaultKafkaConsumerFactory<String, NotificationDto>(consumerProps).createConsumer()) {
            consumerForTest.subscribe(List.of("topic-bankapp-notifications"));

            kafkaTemplate.send("topic-bankapp-notifications", testLogin, notificationDto);

            var receivedMessage = KafkaTestUtils.getSingleRecord(consumerForTest, "topic-bankapp-notifications", Duration.ofSeconds(5));
            assertThat(receivedMessage.key()).isEqualTo(testLogin);
            assertThat(receivedMessage.value().getLogin()).isEqualTo(testLogin);
            assertThat(receivedMessage.value().getMessage()).isEqualTo(testMessage);
        }
    }
}
