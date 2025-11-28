package com.ongi.api.admin.application;

import com.ongi.api.admin.web.dto.CookNutritionItem;
import com.ongi.api.admin.web.dto.CookNutritionResponse;
import com.ongi.api.admin.web.dto.CookRcpResponse;
import com.ongi.api.admin.web.dto.CookRcpRow;
import com.ongi.api.admin.web.dto.GovernmentNutritionRecord;
import com.ongi.api.admin.web.dto.GovernmentNutritionResponse;
import com.ongi.api.admin.web.dto.ParsedIngredient;
import com.ongi.api.ingredients.persistence.IngredientAdapter;
import com.ongi.api.ingredients.persistence.IngredientMapper;
import com.ongi.api.recipe.persistence.RecipeAdapter;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.ingredients.domain.IngredientNutrition;
import com.ongi.ingredients.domain.Nutrition;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;
import com.ongi.ingredients.domain.enums.NutritionBasisEnum;
import com.ongi.ingredients.domain.enums.NutritionEnum;
import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.RecipeTags;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

	Map<NutritionEnum, Nutrition> nutritionCache = new HashMap<>();

	/**
	 * 식품의약품안전처_조리식품 레시피 Import Parser
 	 */
	@Transactional
	public void importSafetyKoreaRecipeFromJson(int fileNo) throws IOException {
		Resource resource = new ClassPathResource("data/식품의약품안전처_조리식품레시피DB_" + fileNo + ".json");
		CookRcpResponse response = objectMapper.readValue(resource.getInputStream(), CookRcpResponse.class);

		for (CookRcpRow row : response.COOKRCP01().row()) {
			Long recipeId = parseToRecipeAndStepsAndTags(row);

			saveRecipeIngredients(recipeId, row.RCP_PARTS_DTLS());
		}
	}

	/**
	 * 식품의약품안전처_영양정보 Import Parser
	 * @throws IOException
	 */
	@Transactional
	public void importSafetyKoreaNutritionFromJson(int fileNo) throws IOException {
		final String base = "data/식품의약품안전처/영양성분/식품의약품안전처_식품영양성분_";
		final String ext = ".json";

		Resource resource = new ClassPathResource(base + fileNo + ext);
		CookNutritionResponse response = objectMapper.readValue(resource.getInputStream(), CookNutritionResponse.class);

		List<IngredientNutrition> ingredientNutritions = new ArrayList<>();

		for(CookNutritionItem item : response.body().items()) {
			IngredientCategoryEnum category = mapCategory(item.FOOD_CAT1_NM(), item.DB_GRP_NM()); // 카테고리
			Double calories = toDouble(item.AMT_NUM1()); // 에너지(kcal)
			Double protein = toDouble(item.AMT_NUM3());  // 단백질(g)
			Double fat = toDouble(item.AMT_NUM4());      // 지방(g)
			Double carbs = toDouble(item.AMT_NUM6());    // 탄수화물(g)

			Ingredient ingredient = ingredientAdapter.findOrCreateIngredient(item.FOOD_NM_KR(), category, calories, protein, fat, carbs);

			Map<NutritionEnum, Double> nutritionValues = extractNutritions(item);
			for (Map.Entry<NutritionEnum, Double> entry : nutritionValues.entrySet()) {
				NutritionEnum code = entry.getKey();
				Double quantity = entry.getValue();
				if (quantity == null || quantity == 0d) continue;
				Nutrition nutrition = nutritionCache.computeIfAbsent(code, ingredientAdapter::findOrCreateNutrition);
				IngredientNutrition ingredientNutrition = IngredientNutrition.create(null, ingredient, nutrition, quantity, deduceBasis(item.SERVING_SIZE()));
				ingredientNutritions.add(ingredientNutrition);

				if(ingredientNutritions.size() >= 1000) {
					ingredientAdapter.saveAllIngredientNutrions(ingredientNutritions);
					ingredientNutritions.clear();
				}
			}
		}

		if (!ingredientNutritions.isEmpty()) {
			ingredientAdapter.saveAllIngredientNutrions(ingredientNutritions);
		}
	}

	/**
	 * 공공데이텊포탈_전국 통합 식품 영양 성분 정보 표준 데이터 Import Parser
	 * @throws IOException
	 */
	@Transactional
	public void importGovernmentNutritionFromJson() throws IOException {
		final String base = "data/전국통합식품영양성분정보표준데이터.json";

		Resource resource = new ClassPathResource(base);
		GovernmentNutritionResponse response = objectMapper.readValue(resource.getInputStream(), GovernmentNutritionResponse.class);

		List<IngredientNutrition> ingredientNutritions = new ArrayList<>();

		for(GovernmentNutritionRecord record : response.records()) {
			String foodName = record.식품코드();
			Double calories = toDouble(record.에너지());
			Double protein = toDouble(record.단백질());
			Double fat = toDouble(record.지방());
			Double carbs = toDouble(record.탄수화물());

			Ingredient ingredient = ingredientAdapter.findOrCreateIngredient(foodName, IngredientCategoryEnum.OTHER, calories, protein, fat, carbs);
			Map<NutritionEnum, Double> nutritionValues = extractNutritions(record);

			for (Map.Entry<NutritionEnum, Double> entry : nutritionValues.entrySet()) {
				NutritionEnum code = entry.getKey();
				Double quantity = entry.getValue();
				if (quantity == null || quantity == 0d) continue;
				Nutrition nutrition = nutritionCache.computeIfAbsent(code, ingredientAdapter::findOrCreateNutrition);
				IngredientNutrition ingredientNutrition = IngredientNutrition.create(null, ingredient, nutrition, quantity, deduceBasis(record.영양성분함량기준량()));
				ingredientNutritions.add(ingredientNutrition);

				if(ingredientNutritions.size() >= 1000) {
					ingredientAdapter.saveAllIngredientNutrions(ingredientNutritions);
					ingredientNutritions.clear();
				}
			}
		}

		if (!ingredientNutritions.isEmpty()) {
			ingredientAdapter.saveAllIngredientNutrions(ingredientNutritions);
		}
	}

	private IngredientCategoryEnum mapCategory(String cat1Name, String dbGroupName) {
		String s = (cat1Name != null && !cat1Name.isBlank()) ? cat1Name : dbGroupName;
		if (s == null) return IngredientCategoryEnum.OTHER;

		s = s.trim();

		return switch (s) {

			// ------------------------
			// 곡류/밥/면/떡/감자 계열
			// ------------------------
			case "밥류",
			     "곡류",
			     "곡류, 서류 제품" -> IngredientCategoryEnum.GRAIN_RICE;

			case "면류",
			     "면 및 만두류" -> IngredientCategoryEnum.GRAIN_NOODLE_DUMPLING;

			case "빵 및 과자류",
			     "과자류·빵류 또는 떡류" -> IngredientCategoryEnum.GRAIN_BREAD_SNACK;

			case "죽 및 스프류" -> IngredientCategoryEnum.GRAIN_PORRIDGE_SOUP;

			case "감자 및 전분류" -> IngredientCategoryEnum.ROOT_POTATO_STARCH;

			// ------------------------
			// 육류 계열
			// ------------------------
			case "육류" -> IngredientCategoryEnum.MEAT_RAW;

			case "식육가공품 및 포장육",
			     "동물성가공식품류",
			     "수·조·어·육류" -> // 복합 가공육류로 보고 MEAT_PROCESSED로
				IngredientCategoryEnum.MEAT_PROCESSED;

			// ------------------------
			// 수산물 계열
			// ------------------------
			case "어패류 및 기타 수산물" -> IngredientCategoryEnum.SEAFOOD_RAW;

			case "수산가공식품류",
			     "젓갈류" -> IngredientCategoryEnum.SEAFOOD_PROCESSED;

			// ------------------------
			// 채소/김치/해조/버섯
			// ------------------------
			case "채소류",
			     "생채·무침류",
			     "나물·숙채류" -> IngredientCategoryEnum.VEGETABLE_FRESH;

			case "김치류",
			     "장아찌·절임류",
			     "절임류 또는 조림류" -> IngredientCategoryEnum.VEGETABLE_PICKLED_KIMCHI;

			case "해조류",
			     "채소, 해조류" -> IngredientCategoryEnum.VEGETABLE_SEAWEED;

			case "버섯류" -> IngredientCategoryEnum.MUSHROOM;

			// ------------------------
			// 과일
			// ------------------------
			case "과일류" -> IngredientCategoryEnum.FRUIT;

			// ------------------------
			// 두류/견과/종실/두부·묵
			// ------------------------
			case "두류" -> IngredientCategoryEnum.LEGUME;

			case "견과 및 종실류",
			     "두류, 견과 및 종실류" -> IngredientCategoryEnum.NUT_SEED;

			case "두부류 또는 묵류" -> IngredientCategoryEnum.TOFU_JELLY;

			// ------------------------
			// 유제품/난류
			// ------------------------
			case "우유류",
			     "유가공품류",
			     "유제품류 및 빙과류" -> IngredientCategoryEnum.DAIRY;

			case "난류",
			     "알가공품류" -> IngredientCategoryEnum.EGG;

			// ------------------------
			// 유지/식용유
			// ------------------------
			case "유지류",
			     "식용유지류" -> IngredientCategoryEnum.OIL_FAT;

			// ------------------------
			// 당류/디저트/간식
			// ------------------------
			case "당류" -> IngredientCategoryEnum.SWEET_SUGAR;

			case "잼류" -> IngredientCategoryEnum.SWEET_JAM;

			case "코코아가공품류 또는 초콜릿류" -> IngredientCategoryEnum.SWEET_CHOCOLATE;

			case "빙과류" -> IngredientCategoryEnum.SWEET_ICE;

			// ------------------------
			// 조미료/장류/소스
			// ------------------------
			case "조미료류",
			     "조미식품" -> IngredientCategoryEnum.SEASONING;

			case "장류",
			     "장류, 양념류" -> IngredientCategoryEnum.SAUCE_PASTE;

			// ------------------------
			// 완성 요리/즉석식품
			// ------------------------
			case "국 및 탕류",
			     "찌개 및 전골류" -> IngredientCategoryEnum.READY_SOUP_STEW;

			case "구이류",
			     "전·적 및 부침류",
			     "튀김류",
			     "볶음류",
			     "조림류" -> IngredientCategoryEnum.READY_MAIN_SIDE;

			case "즉석식품류",
			     "농산가공식품류" -> IngredientCategoryEnum.READY_MEAL;

			// ------------------------
			// 음료/차/주류
			// ------------------------
			case "음료류",
			     "음료 및 차류" -> IngredientCategoryEnum.BEVERAGE_SOFT;

			case "차류" -> IngredientCategoryEnum.BEVERAGE_TEA;

			case "주류" -> IngredientCategoryEnum.BEVERAGE_ALCOHOL;

			// ------------------------
			// 특수 식품
			// ------------------------
			case "특수의료용도식품" -> IngredientCategoryEnum.SPECIAL_MEDICAL;

			case "특수영양식품" -> IngredientCategoryEnum.SPECIAL_NUTRITION;

			// ------------------------
			// 남는 것들/기타
			// ------------------------
			case "기타",
			     "기타식품류" -> IngredientCategoryEnum.OTHER;

			// 혹시 빠진 것들은 일단 OTHER 로
			default -> IngredientCategoryEnum.OTHER;
		};
	}

	private Map<NutritionEnum, Double> extractNutritions(CookNutritionItem r) {
		Map<NutritionEnum, Double> map = new EnumMap<>(NutritionEnum.class);

		map.put(NutritionEnum.ENERGY_KCAL, toDouble(r.AMT_NUM1()));
		map.put(NutritionEnum.PROTEIN, toDouble(r.AMT_NUM3()));
		map.put(NutritionEnum.FAT, toDouble(r.AMT_NUM4()));
		map.put(NutritionEnum.CARBOHYDRATE, toDouble(r.AMT_NUM6()));
		map.put(NutritionEnum.SUGAR, toDouble(r.AMT_NUM7()));
		map.put(NutritionEnum.DIETARY_FIBER, toDouble(r.AMT_NUM8()));
		map.put(NutritionEnum.SATURATED_FAT, toDouble(r.AMT_NUM24()));
		map.put(NutritionEnum.TRANS_FAT, toDouble(r.AMT_NUM25()));
		map.put(NutritionEnum.CHOLESTEROL, toDouble(r.AMT_NUM23()));
		map.put(NutritionEnum.SODIUM, toDouble(r.AMT_NUM13()));

		map.put(NutritionEnum.CALCIUM, toDouble(r.AMT_NUM9()));
		map.put(NutritionEnum.IRON, toDouble(r.AMT_NUM10()));
		map.put(NutritionEnum.POTASSIUM, toDouble(r.AMT_NUM12()));
		map.put(NutritionEnum.MAGNESIUM, toDouble(r.AMT_NUM111()));
		map.put(NutritionEnum.PHOSPHORUS, toDouble(r.AMT_NUM11()));

		map.put(NutritionEnum.VITAMIN_A, toDouble(r.AMT_NUM14()));  // 비타민 A(μg RAE)
		map.put(NutritionEnum.VITAMIN_B1, toDouble(r.AMT_NUM18()));
		map.put(NutritionEnum.VITAMIN_B2, toDouble(r.AMT_NUM19()));
		map.put(NutritionEnum.VITAMIN_B3, toDouble(r.AMT_NUM20())); // 니아신
		map.put(NutritionEnum.VITAMIN_C, toDouble(r.AMT_NUM21()));
		map.put(NutritionEnum.VITAMIN_D, toDouble(r.AMT_NUM22()));
		map.put(NutritionEnum.VITAMIN_E, toDouble(r.AMT_NUM36()));
		map.put(NutritionEnum.VITAMIN_K, toDouble(r.AMT_NUM48()));

		return map;
	}

	private Map<NutritionEnum, Double> extractNutritions(GovernmentNutritionRecord r) {
		Map<NutritionEnum, Double> map = new EnumMap<>(NutritionEnum.class);

		map.put(NutritionEnum.ENERGY_KCAL, toDouble(r.에너지()));
		map.put(NutritionEnum.PROTEIN, toDouble(r.단백질()));
		map.put(NutritionEnum.FAT, toDouble(r.지방()));
		map.put(NutritionEnum.CARBOHYDRATE, toDouble(r.탄수화물()));
		map.put(NutritionEnum.SUGAR, toDouble(r.당류()));
		map.put(NutritionEnum.DIETARY_FIBER, toDouble(r.식이섬유()));
		map.put(NutritionEnum.SATURATED_FAT, toDouble(r.포화지방산()));
		map.put(NutritionEnum.TRANS_FAT, toDouble(r.트랜스지방산()));
		map.put(NutritionEnum.CHOLESTEROL, toDouble(r.콜레스테롤()));
		map.put(NutritionEnum.SODIUM, toDouble(r.나트륨()));

		map.put(NutritionEnum.CALCIUM, toDouble(r.칼슘()));
		map.put(NutritionEnum.IRON, toDouble(r.철분()));
		map.put(NutritionEnum.POTASSIUM, toDouble(r.칼륨()));
		// map.put(NutritionEnum.MAGNESIUM, toDouble(r.마그네슘()));
		map.put(NutritionEnum.PHOSPHORUS, toDouble(r.인()));

		map.put(NutritionEnum.VITAMIN_A, toDouble(r.비타민A()));  // 비타민 A(μg RAE)
		map.put(NutritionEnum.VITAMIN_B1, toDouble(r.티아민()));
		map.put(NutritionEnum.VITAMIN_B2, toDouble(r.리보플라빈()));
		map.put(NutritionEnum.VITAMIN_B3, toDouble(r.니아신())); // 니아신
		map.put(NutritionEnum.VITAMIN_C, toDouble(r.비타민C()));
		map.put(NutritionEnum.VITAMIN_D, toDouble(r.비타민D()));
		//map.put(NutritionEnum.VITAMIN_E, toDouble(r.AMT_NUM36()));
		//map.put(NutritionEnum.VITAMIN_K, toDouble(r.AMT_NUM48()));

		return map;
	}

	private Double toDouble(String v) {
		if (v == null || v.isBlank()) return null;
		try {
			return Double.parseDouble(v);
		} catch (NumberFormatException e) {
			return null;
		}
	}

	/**
	 * 기준(basis) 추론
	 * 대부분 이 API는 100g 기준이므로,
	 * SERVING_SIZE에 "100g" 포함되어 있으면 PER_100G, 아니면 일단 PER_SERVING
	 **/
	private NutritionBasisEnum deduceBasis(String servingSize) {
		String s = servingSize != null ? servingSize.trim() : "";
		if (s.contains("100g") || s.equalsIgnoreCase("100 g")) {
			return NutritionBasisEnum.PER_100G;
		}
		if (s.contains("100ml") || s.equalsIgnoreCase("100 ml")) {
			return NutritionBasisEnum.PER_100ML;
		}
		// 기준 정보를 더 정확히 쓰고 싶으면 여기 로직 확장
		return NutritionBasisEnum.PER_SERVING;
	}

	private Long parseToRecipeAndStepsAndTags(CookRcpRow row) {
		// Create Recipe
		var recipe = recipeAdapter.save(
			Recipe.create(
				null,
				row.RCP_NM(),
				row.RCP_PAT2(),
				row.INFO_WGT() == null ? 1 : Integer.parseInt(row.INFO_WGT()),
				0, // 조리시간 없음
				RecipeDifficultyEnum.LOW,
				row.ATT_FILE_NO_MAIN(),
				row.ATT_FILE_NO_MK(),  // 임시로 모바일 이미지 넣음. TODO Image 별도의 컬럼으로 빼서 사이즈 별로 정의할 수 있도록 할 것.
				"식품의약품안전처"
			)
		);

		// Create Recipe Steps
		List<RecipeSteps> steps = new ArrayList<>();
		for(int i = 1; i <= 20; i++) {
			String desc = row.getManual(i);
			if (desc == null || desc.isBlank()) continue;

			String img  = row.getManualImg(i);

			steps.add(RecipeSteps.create(
				null, recipe.getId(), i, "STEP " + i, desc, null, null, null, img, null
			));
		}
		recipeAdapter.saveAllRecipeSteps(steps);

		// Create Recipe Tags
		List<RecipeTags> tags = new ArrayList<>();
		if (row.HASH_TAG() != null && !row.HASH_TAG().isBlank()) {
			String[] arr = row.HASH_TAG().split(",");
			for (String t : arr) {
				if (t.isBlank()) continue;
				tags.add(RecipeTags.create(null, recipe.getId(), t.trim()));
			}
		}
		recipeAdapter.saveAllRecipeTags(tags);

		return recipe.getId();
	}

	@Transactional
	public void saveRecipeIngredients(Long recipeId, String partsDetails) {
		if (partsDetails == null || partsDetails.isBlank()) return;

		// 줄바꿈/콤마 기준으로 나눔
		String normalized = partsDetails.replace("\n", ",");
		String[] tokens = normalized.split(",");

		List<RecipeIngredient> domains = new ArrayList<>();
		int sortOrder = 1;

		// Ingredient + Recipe Ingredient
		for (String token : tokens) {
			String trimmed = token.trim();
			if (trimmed.isBlank()) continue;

			ParsedIngredient parsed = this.parseIngredient(trimmed);
			if (parsed == null) continue;

			Ingredient ingredient = ingredientAdapter.findLikeOrCreateIngredient(parsed.name(), IngredientCategoryEnum.OTHER, 0.0, 0.0, 0.0, 0.0);

			RecipeIngredient domain = RecipeIngredient.create(null, recipeId, ingredient, parsed.quantity(), parsed.unit(), parsed.note(), sortOrder++);
			domains.add(domain);
		}

		ingredientAdapter.saveAllRecipeIngredients(domains);
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
		Double quantity = 1.0;
		RecipeIngredientUnitEnum unit = RecipeIngredientUnitEnum.TO_TASTE;

		if (m.find()) {
			String quantityStr = m.group(1);
			String unitStr = m.group(2);

			name = base.substring(0, m.start()).trim();
			quantity = Double.parseDouble(quantityStr);
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
					quantity = 1.0;
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
