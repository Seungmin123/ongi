package com.ongi.api.community.application.component;

import org.springframework.stereotype.Component;

@Component
public class StorageKeyFactory {

	public String newTempKey(Long uploaderId, String fileName) {
		String safeName = (fileName == null ? "file" : fileName).replaceAll("[^a-zA-Z0-9._-]", "_");
		return "community/tmp/uploader-" + uploaderId + "/" + java.util.UUID.randomUUID() + "-" + safeName;
	}
}
