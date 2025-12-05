package com.ongi.api.recipe.application;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.recipe.persistence.RecipeAdapter;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.search.RecipeSearch;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RecipeService {

	private final RecipeAdapter recipeAdapter;

	public ApiResponse<List<RecipeCardResponse>> search(
		CursorPageRequest cursorPageRequest,
		RecipeSearch search
	) {
		RecipeSearchCondition condition = mapToCondition(search);
		Long cursor = cursorPageRequest.cursor();
		int size = cursorPageRequest.size();
		PageSortOptionEnum sort = cursorPageRequest.resolveSort();

        return ApiResponse.ok();
		//return recipeAdapter.search(condition, cursor, size, sort);
	}

	private RecipeSearchCondition mapToCondition(RecipeSearch search) {
		return switch (search) {
			case RecipeSearch.ByKeyword s -> new RecipeSearchCondition(
				s.keyword(), null, null, null, null
			);

			case RecipeSearch.ByTag s -> new RecipeSearchCondition(
				null, s.tag(), null, null, null
			);

			case RecipeSearch.ByCategory s -> new RecipeSearchCondition(
				null, null, s.category(), null, null
			);

			case RecipeSearch.ByIngredient s -> new RecipeSearchCondition(
				null, null, null, s.ingredientId(), null
			);

			case RecipeSearch.ByMaxCookingTimeMin s -> new RecipeSearchCondition(
				null, null, null, null, s.maxCookingTimeMin()
			);

			case RecipeSearch.ByComplex s -> new RecipeSearchCondition(
				s.keyword(), s.tag(), s.category(), s.ingredientId(), s.maxCookingTimeMin()
			);
		};
	}

}
