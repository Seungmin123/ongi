package com.ongi.api.config.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class UserRedisTemplate {

	@Bean
	public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
		return new StringRedisTemplate(cf);
	}

	public static String sha256(String raw) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
			return HexFormat.of().formatHex(digest);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
