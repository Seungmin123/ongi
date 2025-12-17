package com.ongi.api.config.cache.store.user;

import com.ongi.api.common.web.dto.UploadMeta;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UploadSessionStore {

	private final StringRedisTemplate redis;

	private static final String KEY_PREFIX = "upload:profile:";

	private static final String F_STATUS = "status";
	private static final String F_OBJECT_KEY = "objectKey";
	private static final String F_CONTENT_TYPE = "contentType";
	private static final String F_CONTENT_LENGTH = "contentLength";

	public static final String S_PRESIGNED = "PRESIGNED";
	public static final String S_UPLOADED = "UPLOADED";
	public static final String S_CONSUMED = "CONSUMED";

	// return:
	//  1 = ok
	//  0 = not exists
	// -1 = status not UPLOADED
	// -2 = objectKey mismatch
	private static final DefaultRedisScript<Long> CONSUME_SCRIPT = new DefaultRedisScript<>(
		"""
		local key = KEYS[1]
		local expectedObjectKey = ARGV[1]

		if (redis.call('EXISTS', key) == 0) then
		  return 0
		end

		local status = redis.call('HGET', key, 'status')
		if (status ~= 'UPLOADED') then
		  return -1
		end

		local objectKey = redis.call('HGET', key, 'objectKey')
		if (objectKey ~= expectedObjectKey) then
		  return -2
		end

		redis.call('HSET', key, 'status', 'CONSUMED')
		return 1
		""",
		Long.class
	);

	private String key(String tokenHash) {
		return KEY_PREFIX + tokenHash;
	}

	public void savePresigned(String tokenHash, String objectKey, String contentType, long contentLength, long ttlSeconds) {
		String k = key(tokenHash);
		redis.opsForHash().put(k, F_STATUS, S_PRESIGNED);
		redis.opsForHash().put(k, F_OBJECT_KEY, objectKey);
		redis.opsForHash().put(k, F_CONTENT_TYPE, contentType);
		redis.opsForHash().put(k, F_CONTENT_LENGTH, String.valueOf(contentLength));
		redis.expire(k, Duration.ofSeconds(ttlSeconds));
	}

	public void markUploaded(String tokenHash) {
		String k = key(tokenHash);
		redis.opsForHash().put(k, F_STATUS, S_UPLOADED);
	}

	public boolean consumeIfUploaded(String tokenHash, String objectKey) {
		String k = key(tokenHash);
		Long r = redis.execute(CONSUME_SCRIPT, List.of(k), objectKey);
		return r != null && r == 1L;
	}

	public UploadMeta getMeta(String tokenHash) {
		String k = key(tokenHash);
		if (Boolean.FALSE.equals(redis.hasKey(k))) return null;

		Object objectKey = redis.opsForHash().get(k, F_OBJECT_KEY);
		Object contentType = redis.opsForHash().get(k, F_CONTENT_TYPE);
		Object contentLength = redis.opsForHash().get(k, F_CONTENT_LENGTH);
		Object status = redis.opsForHash().get(k, F_STATUS);

		if (objectKey == null || contentType == null || contentLength == null || status == null) return null;

		long len;
		try {
			len = Long.parseLong(contentLength.toString());
		} catch (NumberFormatException e) {
			return null;
		}

		return new UploadMeta(objectKey.toString(), contentType.toString(), len, status.toString());
	}

}
