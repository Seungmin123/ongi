package com.ongi.api.common.messaging.publisher;

import com.ongi.api.common.persistence.entity.OutBoxEventEntity;
import com.ongi.api.common.persistence.entity.repository.OutBoxEventRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutBoxPublisherTx {

	private final OutBoxEventRepository outBoxEventRepository;

	@Value("${spring.instance-id:${random.uuid}}")
	private String instanceId;

	private static final int MAX_RETRY = 20;

	@Transactional(transactionManager = "transactionManager")
	public void reclaimStale(int staleSeconds) {
		outBoxEventRepository.reclaimStale(staleSeconds);
	}

	@Transactional(transactionManager = "transactionManager")
	public List<Long> claim(int limit) {
		List<Long> ids = outBoxEventRepository.findPendingIdsForUpdateSkipLocked(limit);
		if (ids.isEmpty()) return ids;

		outBoxEventRepository.markProcessing(ids, instanceId);
		return ids;
	}

	@Transactional(transactionManager = "transactionManager")
	public void markSuccess(long id) {
		outBoxEventRepository.markSuccess(id);
	}

	@Transactional(transactionManager = "transactionManager")
	public void handleFailure(OutBoxEventEntity e, Throwable ex) {
		Throwable cause = unwrap(ex);
		String err = safeError(cause);

		int retry = e.getRetryCount();

		if (retry >= MAX_RETRY) {
			outBoxEventRepository.markFailed(e.getId(), err);
			return;
		}

		LocalDateTime nextAttempt = LocalDateTime.now().plusNanos(backoffMs(retry) * 1_000_000L);
		outBoxEventRepository.markRetry(e.getId(), err, nextAttempt);
	}

	private long backoffMs(int retryCount) {
		long base = 1000L * (1L << Math.min(retryCount, 10)); // 1s,2s,4s... 최대 ~1024s
		long capped = Math.min(base, 300_000L);               // 5분 cap
		long jitter = ThreadLocalRandom.current().nextLong(0, 500);
		return capped + jitter;
	}

	private Throwable unwrap(Throwable t) {
		// CompletableFuture에서 흔하게 감싸는 래퍼들 벗기기
		while (t instanceof java.util.concurrent.CompletionException
			|| t instanceof java.util.concurrent.ExecutionException) {
			if (t.getCause() == null) break;
			t = t.getCause();
		}
		return t;
	}

	private String safeError(Throwable ex) {
		String msg = ex.getClass().getSimpleName() + ": " + (ex.getMessage() == null ? "" : ex.getMessage());

		// root cause 메시지도 붙여서 운영 디버깅 쉽게
		Throwable root = ex;
		while (root.getCause() != null && root.getCause() != root) {
			root = root.getCause();
		}
		if (root != ex) {
			String rootMsg = root.getClass().getSimpleName() + ": " + (root.getMessage() == null ? "" : root.getMessage());
			msg = msg + " | root=" + rootMsg;
		}

		return msg.length() > 1000 ? msg.substring(0, 1000) : msg;
	}
}
