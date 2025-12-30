package com.ongi.api.user.port;

import com.ongi.ingredients.domain.AllergenGroup;
import com.ongi.ingredients.domain.Ingredient;
import java.util.Collection;
import java.util.Set;

public interface IngredientsProvider {

	Set<Ingredient> findIngredientsByIds(Collection<Long> ingredientIds);

	Set<AllergenGroup> findAllergenGroupsByIds(Collection<Long> allergyIds);
}
