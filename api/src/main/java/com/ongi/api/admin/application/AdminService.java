package com.ongi.api.admin.application;

import com.ongi.api.admin.web.dto.CookRcpResponse;
import com.ongi.api.admin.web.dto.CookRcpRow;
import com.ongi.api.admin.web.dto.ParsedIngredient;
import com.ongi.api.ingredients.persistence.IngredientAdapter;
import com.ongi.api.ingredients.persistence.IngredientMapper;
import com.ongi.api.recipe.persistence.RecipeAdapter;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Service
public class AdminService {

	private final IngredientAdapter ingredientAdapter;

	private final RecipeAdapter recipeAdapter;

	private final ObjectMapper objectMapper;

	private static final String[] NOTE_KEYWORDS = {"다진", "채썬", "송송 썬", "송송썬", "다져", "곱게 다진"};

	// 식품의약품안전처_조리식품 레시피
	@Transactional
	public void importFromJson(String classPathResource) throws IOException {
		//CookRcpResponse response = objectMapper.readValue(json, CookRcpResponse.class);

		Resource resource = new ClassPathResource("data/식품의약품안전처_조리식품레시피DB_1.json");
		//Resource resource = new ClassPathResource(classPathResource);
		CookRcpResponse response = objectMapper.readValue(resource.getInputStream(), CookRcpResponse.class);

		for (CookRcpRow row : response.COOKRCP01().row()) {
			System.out.println(row);
			// TODO
			// Long recipeId = parseToRecipe(row);
			// save recipe
			// save nutrition
			// save ingredient
			// save recipe ingredient
			// savve nutrition ingredient
			// save recipe steps
			// save recipe tags
			// saveIngredients(recipeId, row.RCP_PARTS_DTLS());
			// saveSteps(recipeId, row); ...
		}
	}

	private Long parseToRecipe(CookRcpRow row) {
		// 도메인 만들기
		var recipe = recipeAdapter.save(
			Recipe.create(
				row.RCP_NM(),
				row.RCP_PARTS_DTLS(),
				Integer.parseInt(row.INFO_WGT()),
				0, // 조리시간 없음
				RecipeDifficultyEnum.LOW,
				"식품의약품안전처"
			)
		);

		return recipe.getId();
	}

	@Transactional
	public void saveIngredients(Long recipeId, String partsDetails) {
		if (partsDetails == null || partsDetails.isBlank()) return;

		// 줄바꿈/콤마 기준으로 나눔
		String normalized = partsDetails.replace("\n", ",");
		String[] tokens = normalized.split(",");

		List<RecipeIngredient> domains = new ArrayList<>();
		int sortOrder = 1;

		for (String token : tokens) {
			String trimmed = token.trim();
			if (trimmed.isBlank()) continue;

			ParsedIngredient parsed = this.parseIngredient(trimmed);
			if (parsed == null) continue;

			Ingredient ingredient = ingredientAdapter.findOrCreateIngredient(parsed.name());

			RecipeIngredient domain = RecipeIngredient.create(recipeId, ingredient, parsed.quantity(), parsed.unit(), parsed.note(), sortOrder++);

			domains.add(domain);
		}

		ingredientAdapter.saveAll(domains);
	}

	public ParsedIngredient parseIngredient(String rawText) {
		if (rawText == null || rawText.isBlank()) {
			return null;
		}

		String text = rawText.trim();

		// 1) 괄호 안은 note 후보로 분리
		//    "연두부 75g(3/4모)" -> base: "연두부 75g", note: "3/4모"
		String note = null;
		String base = text;

		Matcher parenMatcher = Pattern.compile("\\((.*?)\\)").matcher(text);
		if (parenMatcher.find()) {
			note = parenMatcher.group(1).trim();
			base = text.substring(0, parenMatcher.start()).trim();
		}

		// 2) 전처리(다진/채썬 등) note 분리
		for (String keyword : NOTE_KEYWORDS) {
			if (base.startsWith(keyword)) {
				String remaining = base.substring(keyword.length()).trim(); // "대파 1큰술"
				note = (note == null) ? keyword : (keyword + ", " + note);
				base = remaining;
				break;
			} else {
				System.out.println(base);
			}
		}

		// 3) 숫자 + 단위 추출
		//  "연두부 75g" -> name: 연두부, quantity: 75, unitStr: g
		//  "간장 약간"   -> quantity 없음, 단위/노트 처리
		Pattern p = Pattern.compile("([0-9]+\\.?[0-9]*)\\s*([a-zA-Z가-힣°]+)");
		Matcher m = p.matcher(base);

		String name;
		Integer quantity = 1;
		RecipeIngredientUnitEnum unit = RecipeIngredientUnitEnum.TO_TASTE;

		if (m.find()) {
			String quantityStr = m.group(1);
			String unitStr = m.group(2);

			name = base.substring(0, m.start()).trim();
			quantity = (int) Math.round(Double.parseDouble(quantityStr));
			unit = IngredientMapper.mapUnit(unitStr);
		} else {
			// 숫자 패턴이 없으면
			// "간장 약간" 같은 케이스 처리
			String[] tokens = base.split("\\s+");
			if (tokens.length >= 2) {
				name = tokens[0].trim();
				String maybeUnitOrNote = tokens[1].trim();

				RecipeIngredientUnitEnum mappedUnit = IngredientMapper.mapUnit(maybeUnitOrNote);
				if (mappedUnit == RecipeIngredientUnitEnum.DASH
					|| mappedUnit == RecipeIngredientUnitEnum.PINCH
					|| mappedUnit == RecipeIngredientUnitEnum.TO_TASTE) {

					unit = mappedUnit;
					quantity = 1;
					note = (note == null) ? maybeUnitOrNote : (note + ", " + maybeUnitOrNote);
				} else {
					// 단위로 애매하면 통째로 이름으로
					name = base;
				}
			} else {
				name = base;
			}
		}

		if (note == null) {
			note = "";
		}

		return new ParsedIngredient(name, quantity, unit, note);
	}

}
