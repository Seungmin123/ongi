package com.ongi.api.config.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer.GenericJacksonJsonRedisSerializerBuilder;
import org.springframework.data.redis.serializer.RedisSerializationContext;

@EnableCaching
@Configuration
public class CacheManagerConfig {

	@Bean
	@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true")
	public RedisConnectionFactory redisConnectionFactory(
		@Value("${spring.data.redis.host}") String host,
		@Value("${spring.data.redis.port}") int port) {
		return new LettuceConnectionFactory(host, port);
	}

	@Bean
	@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "true")
	public CacheManager redisCacheManager(RedisConnectionFactory factory) {
		GenericJacksonJsonRedisSerializer serializer =
			GenericJacksonJsonRedisSerializer.create(
				GenericJacksonJsonRedisSerializerBuilder::enableUnsafeDefaultTyping);

		RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
			.prefixCacheNameWith("ongi::") // 전역 prefix
			.entryTtl(Duration.ofMinutes(10)) // 기본 TTL 설정
			.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
			.disableCachingNullValues();

		return RedisCacheManager.builder(factory)
			.cacheDefaults(config)
			.withInitialCacheConfigurations(perCacheTTL(serializer)) // 캐시별 TTL 개별 설정
			.build();
	}

	@Bean
	@Profile("local")
	@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "false", matchIfMissing = true)
	public CacheManager LocalCacheManager() {
		return new ConcurrentMapCacheManager(CacheSpec.cacheNames());
	}

	@Bean
	@Profile("!local")
	@ConditionalOnProperty(name = "spring.data.redis.enabled", havingValue = "false", matchIfMissing = true)
	public CacheManager noOpCacheManager() {
		return new NoOpCacheManager();
	}

	private Map<String, RedisCacheConfiguration> perCacheTTL(GenericJacksonJsonRedisSerializer serializer) {
		RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
			.serializeValuesWith(
				RedisSerializationContext.SerializationPair.fromSerializer(serializer)
			)
			.disableCachingNullValues();

		return CacheSpec.ttlMap().entrySet().stream()
			.collect(Collectors.toMap(
				Map.Entry::getKey,
				e -> baseConfig.entryTtl(e.getValue())
			));
	}

}
