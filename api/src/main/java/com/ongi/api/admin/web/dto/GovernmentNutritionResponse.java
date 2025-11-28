package com.ongi.api.admin.web.dto;

import java.util.List;

public record GovernmentNutritionResponse (
	List<GovernmentNutritionField> fields,
	List<GovernmentNutritionRecord> records
) {

}
