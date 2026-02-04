package com.ongi.api.config.cache;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

	@Bean
	@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true") // Redis가 활성화된 경우에만 생성
	public RedissonClient redissonClient(
		@Value("${spring.data.redis.host}") String host,
		@Value("${spring.data.redis.port}") int port) {

		Config config = new Config();
		config.useSingleServer()
			.setAddress("redis://" + host + ":" + port);
		// 비밀번호가 있다면 .setPassword(...) 추가

		return Redisson.create(config);
	}

	@Bean
	public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, String> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);

		StringRedisSerializer serializer = new StringRedisSerializer();

		template.setKeySerializer(serializer);
		template.setValueSerializer(serializer);
		template.setHashKeySerializer(serializer);
		template.setHashValueSerializer(serializer);

		template.afterPropertiesSet();
		return template;
	}

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
		return new StringRedisTemplate(cf);
	}
}
