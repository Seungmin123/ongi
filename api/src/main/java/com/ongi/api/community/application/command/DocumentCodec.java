package com.ongi.api.community.application.command;

import java.util.Set;

public interface DocumentCodec {

	Set<Long> extractAttachmentIds(String contentJson);

	String extractPlainText(String contentJson);
}
