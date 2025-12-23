package com.ongi.api.community.adatper.out.file;

import com.ongi.api.community.web.dto.AttachmentDto;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbAttachmentClient implements AttachmentReadClient{

	@Override
	public Map<Long, AttachmentDto> getAttachmentsByIds(Set<Long> userIds) {
		return Map.of();
	}
}
