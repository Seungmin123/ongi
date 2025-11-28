package com.ongi.api.admin.web;

import com.ongi.api.admin.application.AdminService;
import com.ongi.api.common.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/private/admin")
@RestController
public class AdminController {

	private final AdminService adminService;


	@PostMapping("/foodSafetyKorea/recipe")
	public String foodSafetyKoreaRecipe() throws Exception{
		for(int i = 1; i <= 2; i++) {
			adminService.importSafetyKoreaRecipeFromJson(i);
		}
		return "ok";
	}

	@PostMapping("/foodSafetyKorea/nutrition")
	public String foodSafetyKoreaNutrition() throws Exception{
		for(int i = 1; i <= 335; i++) {
			adminService.importSafetyKoreaNutritionFromJson(i);
		}
		return "ok";
	}

	@PostMapping("/government/nutrition")
	public ApiResponse<Void> governmentNutrition() throws Exception{
		adminService.importGovernmentNutritionFromJson();
		return ApiResponse.ok();
	}
}
