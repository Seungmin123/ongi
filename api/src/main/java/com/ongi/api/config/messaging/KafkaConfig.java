//package com.ongi.api.config.messaging;
//
//import java.util.Map;
//
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//
//@Configuration
//@EnableKafka
//public class KafkaConfig {
//
//	private final KafkaProperties kafkaProperties;
//
//	public KafkaConfig(KafkaProperties kafkaProperties) {
//		this.kafkaProperties = kafkaProperties;
//	}
//
//	@Bean
//	public ProducerFactory<String, String> producerFactory() {
//		// application.yml의 spring.kafka.producer 설정을 그대로 가져옴
//		Map<String, Object> props = kafkaProperties.buildProducerProperties();
//
//		// 혹시 추가로 강제하고 싶은 옵션이 있으면 여기에서 덧씌우면 됨
//		props.putIfAbsent(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//		props.putIfAbsent(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//
//		return new DefaultKafkaProducerFactory<>(props);
//	}
//
//	@Bean
//	public KafkaTemplate<String, String> kafkaTemplate() {
//		return new KafkaTemplate<>(producerFactory());
//	}
//}
