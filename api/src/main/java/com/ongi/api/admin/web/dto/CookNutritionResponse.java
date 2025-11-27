package com.ongi.api.admin.web.dto;

/**
 * 식품의약품안전처_식품의약성분 DTO
 */
public record CookNutritionResponse(
	CookNutritionHeader header,
	CookNutritionBody body
) {}
