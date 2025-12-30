package com.ongi.api.user.adapter.out.ingredients;

import com.ongi.api.ingredients.adapter.out.persistence.IngredientAdapter;
import com.ongi.api.user.port.IngredientsProvider;
import com.ongi.ingredients.domain.AllergenGroup;
import com.ongi.ingredients.domain.Ingredient;
import java.util.Collection;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IngredientsDbProvider implements IngredientsProvider {

	private final IngredientAdapter ingredientAdapter;

	@Override
	public Set<Ingredient> findIngredientsByIds(Collection<Long> ingredientIds) {
		return ingredientAdapter.findIngredientsByIds(ingredientIds);
	}

	@Override
	public Set<AllergenGroup> findAllergenGroupsByIds(Collection<Long> allergyIds) {
		return ingredientAdapter.findAllergenGroupsByIds(allergyIds);
	}
}
