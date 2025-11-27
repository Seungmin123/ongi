package com.ongi.api.admin.web;

import com.ongi.api.admin.application.AdminService;
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
		adminService.importSafetyKoreaRecipeFromJson("");
		return "ok";
	}

	@PostMapping("/foodSafetyKorea/nutrition")
	public String foodSafetyKoreaNutrition() throws Exception{
		adminService.importSafetyKoreaNutritionFromJson("");
		return "ok";
	}
}
