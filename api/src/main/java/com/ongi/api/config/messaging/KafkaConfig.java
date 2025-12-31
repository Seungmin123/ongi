//package com.ongi.api.config.messaging;
//
//import com.ongi.api.user.application.event.UserEventMessage;
//import java.util.HashMap;
//import java.util.Map;
//
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.StringSerializer;
//import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.kafka.annotation.EnableKafka;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//
//@Configuration
//@EnableKafka
//public class KafkaConfig {
//
//	@Bean
//	public ProducerFactory<String, UserEventMessage> userEventProducerFactory(KafkaProperties props) {
//		Map<String, Object> config = new HashMap<>(props.buildProducerProperties());
//		config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
//		config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//
//		// 헤더에 타입정보 넣기 싫으면 false(consumer가 스키마 고정이면 false 권장)
//		config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
//
//		return new DefaultKafkaProducerFactory<>(config);
//	}
//
//	@Bean
//	public KafkaTemplate<String, UserEventMessage> userEventKafkaTemplate(
//		ProducerFactory<String, UserEventMessage> pf
//	) {
//		return new KafkaTemplate<>(pf);
//	}
//}
