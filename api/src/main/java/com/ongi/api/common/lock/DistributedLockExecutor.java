package com.ongi.api.common.lock;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DistributedLockExecutor {

	private final RedissonClient redissonClient;

	public <T> T execute(String lockKey, long waitTime, long leaseTime, Supplier<T> supplier) {
		RLock lock = redissonClient.getLock(lockKey);
		try {
			boolean available = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
			if (!available) {
				throw new IllegalStateException("Lock 획득 실패: " + lockKey);
			}
			return supplier.get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		} finally {
			if (lock.isHeldByCurrentThread()) {
				lock.unlock();
			}
		}
	}
}
