package com.ongi.api.recipe.adapter.out.persistence.repository;

import com.ongi.api.recipe.adapter.out.persistence.QRecipeEntity;
import com.ongi.api.recipe.adapter.out.persistence.projection.RecipeCardRow;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RecipeCardQueryRepository {

	private final JPAQueryFactory queryFactory;

	public Map<Long, RecipeCardRow> findCardsByIds(Set<Long> recipeIds) {
		if (recipeIds.isEmpty()) return Map.of();

		QRecipeEntity r = QRecipeEntity.recipeEntity;

		List<RecipeCardRow> rows = queryFactory
			.select(Projections.constructor(
				RecipeCardRow.class,
				r.id,
				r.title,
				r.imageUrl,
				r.cookingTimeMin,
				r.difficulty.stringValue(),
				r.category.stringValue()
			))
			.from(r)
			.where(r.id.in(recipeIds))
			.fetch();

		return rows.stream().collect(java.util.stream.Collectors.toMap(RecipeCardRow::recipeId, x -> x));
	}

}
