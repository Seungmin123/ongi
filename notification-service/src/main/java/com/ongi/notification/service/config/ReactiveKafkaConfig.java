package com.ongi.notification.service.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class ReactiveKafkaConfig {

    @Value("${spring.kafka.consumer.bootstrap-servers}")
    private String bootstrapServers;

    //@Value("${spring.kafka.consumer.group-id:notification-service-group}")
    @Value("${group-id:notification-service-group}")
    private String groupId;

    @Bean
    public ReceiverOptions<String, String> kafkaReceiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        ReceiverOptions<String, String> basicOptions = ReceiverOptions.create(props);

        return basicOptions.subscription(List.of("order.paid", "notification.request"));
    }

    @Bean
    public KafkaReceiver<String, String> kafkaReceiver(ReceiverOptions<String, String> options) {
        return KafkaReceiver.create(options);
    }
}
