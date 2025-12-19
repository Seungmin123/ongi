package com.ongi.api.recipe.port;

import com.ongi.api.recipe.web.dto.UserSummary;
import java.util.Map;
import java.util.Set;

public interface UserInfoProvider {

	Map<Long, UserSummary> getUsersByIds(Set<Long> userIds);

}
