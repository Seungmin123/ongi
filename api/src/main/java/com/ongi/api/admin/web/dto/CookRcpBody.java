package com.ongi.api.admin.web.dto;

import java.util.List;

public record CookRcpBody(
	String total_count,
	List<CookRcpRow> row
) {}
