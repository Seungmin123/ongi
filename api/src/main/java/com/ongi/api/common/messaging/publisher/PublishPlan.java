package com.ongi.api.common.messaging.publisher;

import java.util.List;

public record PublishPlan(List<PublishTarget> targets) {
	boolean isEmpty() {
		return targets.isEmpty();
	}
}
