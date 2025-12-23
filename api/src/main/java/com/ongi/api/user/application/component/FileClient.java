package com.ongi.api.user.application.component;

import com.ongi.api.user.application.component.impl.LocalFileClient.PresignMeta;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;

public interface FileClient {

	URL presignPut(String objectKey, String contentType, long contentLength, Duration ttl);

	boolean head(String filePath);

	void upload(String filePath, String fileName, InputStream inputStream);

	void promote(String fromObjectKey, String toObjectKey);

	// TODO 임시
	PresignMeta requireValidMeta(String token);

	void consume(String token);

	String generateSignedUrl(String storageKey, Integer minute);
}
