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

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:notification-service-group}")
    private String groupId;

    @Bean
    public ReceiverOptions<String, String> baseReceiverOptions() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return ReceiverOptions.create(props);
    }

    @Bean
    public KafkaReceiver<String, String> realTimeKafkaReceiver(ReceiverOptions<String, String> baseOptions) {
        ReceiverOptions<String, String> options = baseOptions.subscription(List.of("order.paid", "notification.request"));
        return KafkaReceiver.create(options);
    }

    @Bean
    public KafkaReceiver<String, String> broadcastKafkaReceiver(ReceiverOptions<String, String> baseOptions) {
        // Broadcast는 처리가 오래 걸릴 수 있으므로 별도 그룹을 사용하는 것이 안전할 수 있으나,
        // 여기서는 같은 그룹 내에서 토픽만 분리하여 처리 (필요시 group-id 오버라이딩 가능)
        ReceiverOptions<String, String> options = baseOptions.subscription(List.of("notification.broadcast"));
        return KafkaReceiver.create(options);
    }
}
