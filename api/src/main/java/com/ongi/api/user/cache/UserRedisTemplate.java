package com.ongi.api.user.cache;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserRedisTemplate {

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
