package com.ongi.api.admin.web.dto;

import java.util.List;

public record CookNutritionBody(
	Integer pageNo,
	Integer totalCount,
	Integer numOfRows,
	List<CookNutritionItem> items
) {}
