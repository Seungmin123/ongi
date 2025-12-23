package com.ongi.api.community.adatper.out.file;

import com.ongi.api.community.web.dto.AttachmentDto;
import java.util.Map;
import java.util.Set;

public interface AttachmentReadClient {
	Map<Long, AttachmentDto> getAttachmentsByIds(Set<Long> userIds);
}
