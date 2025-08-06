package ru.mdemidkin.notifications.config;

import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.Map;

@TestConfiguration
public class KafkaTestConfig {

    @Bean
    public ProducerFactory<String, Object> producerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
        Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
        props.put("key.serializer", StringSerializer.class);
        props.put("value.serializer", JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConsumerFactory<String, Object> consumerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
        Map<String, Object> props = KafkaTestUtils.consumerProps("service-notifications-test.notifications-consumer-group", "false", embeddedKafkaBroker);
        props.put("key.deserializer", org.apache.kafka.common.serialization.StringDeserializer.class);
        props.put("value.deserializer", org.springframework.kafka.support.serializer.JsonDeserializer.class);
        props.put("spring.json.trusted.packages", "ru.mdemidkin.*");
        props.put("auto.offset.reset", "earliest");
        return new DefaultKafkaConsumerFactory<>(props);
    }
}
