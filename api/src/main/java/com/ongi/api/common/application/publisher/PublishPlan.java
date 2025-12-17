package com.ongi.api.common.application.publisher;

import java.util.List;

public record PublishPlan(List<PublishTarget> targets) {
	boolean isEmpty() {
		return targets.isEmpty();
	}
}
