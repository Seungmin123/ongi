package com.ongi.api.community.adatper.out.cache;

import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostStatsRepository;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PostViewFlushScheduler {

	// Recipe에도 있지만, MSA 전환을 고려하여 각자 만듦

	private static final String KEY = "community:post:view:delta";

	private final StringRedisTemplate redis;

	private final CommunityPostStatsRepository postStatsRepository;

	@Scheduled(fixedDelayString = "300000") // 5분
	@Transactional(transactionManager = "transactionManager")
	public void flushToDb() {
		String processingKey = KEY + ":processing:" + LocalDateTime.now() + ":" + UUID.randomUUID();

		// 원자적 스왑: KEY가 존재하는지 체크 후 processingKey로 이동
		Boolean renamed = redis.execute((RedisCallback<Boolean>) connection -> {
			byte[] src = redis.getStringSerializer().serialize(KEY);
			byte[] dst = redis.getStringSerializer().serialize(processingKey);

			Boolean exists = connection.keyCommands().exists(src);
			if (exists == null || !exists) return false;

			return connection.keyCommands().renameNX(src, dst);
		});

		if (renamed == null || !renamed) {
			// 처리할 게 없거나, 이미 다른 flush가 잡았음
			return;
		}

		// moved 된 processingKey에서 전부 읽기
		Map<Object, Object> entries = redis.opsForHash().entries(processingKey);
		if (entries == null || entries.isEmpty()) {
			redis.delete(processingKey);
			return;
		}

		// DB 배치 반영
		for (Map.Entry<Object, Object> e : entries.entrySet()) {
			long recipeId = Long.parseLong(String.valueOf(e.getKey()));
			long delta = Long.parseLong(String.valueOf(e.getValue()));
			postStatsRepository.incrementViewCount(recipeId, delta);
		}

		// 처리 완료 후 삭제
		redis.delete(processingKey);
	}
}
