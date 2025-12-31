package com.ongi.api.user.application.event;

import com.ongi.api.user.adapter.out.cache.store.UserEventDedupStore;
import com.ongi.api.user.web.dto.UserEventBatchRequest;
import com.ongi.api.user.web.dto.UserEventDto;
import com.ongi.user.domain.enums.UserEventType;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserEventService {

	// 중복 방지 + 재전송 안전 둘 다 커버
	private static final Duration DEDUP_TTL = Duration.ofDays(7);

	private final UserEventProducer producer;

	private final UserEventDedupStore dedupStore;

	public void ingest(Long userId, UserEventBatchRequest req) {

		for (UserEventDto e : req.events()) {

			// 타입별 필수값 검증(레시피 이벤트는 recipeId 필요)
			if (requiresRecipeId(e.type()) && e.recipeId() == null) {
				continue;
			}

			//  eventId 단위 중복 방지 / 멱등성 확보
			boolean first = dedupStore.tryConsume(userId, e.eventId(), DEDUP_TTL);
			if (!first) continue;

			producer.publish(userId, req, e);
		}
	}

	private boolean requiresRecipeId(UserEventType type) {
		return switch (type) {
			case RECIPE_VIEW_START, RECIPE_VIEW_END, RECIPE_ENGAGEMENT -> true;
		};
	}
}
