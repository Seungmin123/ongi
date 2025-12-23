package com.ongi.api.community.adatper.out.user;

import java.util.Map;
import java.util.Set;

public interface UserInfoProvider {

	Map<Long, UserSummary> getUserSummaries(Set<Long> userIds);

}
