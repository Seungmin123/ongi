package com.ongi.api.admin.application;

import com.ongi.api.admin.web.dto.CookNutritionItem;
import com.ongi.api.admin.web.dto.CookNutritionResponse;
import com.ongi.api.admin.web.dto.CookRcpResponse;
import com.ongi.api.admin.web.dto.CookRcpRow;
import com.ongi.api.admin.web.dto.GovernmentNutritionRecord;
import com.ongi.api.admin.web.dto.GovernmentNutritionResponse;
import com.ongi.api.admin.web.dto.ParsedIngredient;
import com.ongi.api.ingredients.adapter.out.persistence.IngredientAdapter;
import com.ongi.api.ingredients.adapter.out.persistence.IngredientMapper;
import com.ongi.api.recipe.adapter.out.persistence.RecipeAdapter;
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
import com.ongi.recipe.domain.enums.RecipeCategoryEnum;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;
import jakarta.persistence.EntityManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	Map<NutritionEnum, Nutrition> nutritionCache = new HashMap<>();

	private final EntityManager em;

	/**
	 * 식품의약품안전처_조리식품 레시피 Import Parser
 	 */
	@Transactional
	public void importSafetyKoreaRecipeFromJson(int fileNo) throws IOException {
		Resource resource = new ClassPathResource("data/식품의약품안전처/조리식품_레시피/식품의약품안전처_조리식품레시피DB_" + fileNo + ".json");
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

			Ingredient ingredient = ingredientAdapter.findOrCreateIngredient(item.FOOD_NM_KR(), item.FOOD_CD(), category, calories, protein, fat, carbs);

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
			String foodName = record.식품명();
			String foodCode = record.식품코드();
			Double calories = toDouble(record.에너지());
			Double protein = toDouble(record.단백질());
			Double fat = toDouble(record.지방());
			Double carbs = toDouble(record.탄수화물());

			Ingredient ingredient = ingredientAdapter.findOrCreateIngredient(foodName, foodCode, IngredientCategoryEnum.OTHER, calories, protein, fat, carbs);
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
					em.flush();

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
				null,
				row.RCP_NM(),
				null,
				row.INFO_WGT() == null|| row.INFO_WGT().isEmpty() ? 1 : Double.parseDouble(row.INFO_WGT()),
				0, // 조리시간 없음
				RecipeDifficultyEnum.LOW,
				row.ATT_FILE_NO_MAIN(),
				row.ATT_FILE_NO_MK(),  // 임시로 모바일 이미지 넣음. TODO Image 별도의 컬럼으로 빼서 사이즈 별로 정의할 수 있도록 할 것.
				"식품의약품안전처",
				RecipeCategoryEnum.from(row.RCP_PAT2())
			)
		);

		// Create Recipe Steps
		List<RecipeSteps> steps = new ArrayList<>();
		for(int i = 1; i <= 20; i++) {
			String desc = row.getManual(i);
			if (desc == null || desc.isBlank()) continue;

			String img  = row.getManualImg(i);

			steps.add(RecipeSteps.create(
				null, recipe.getId(), i, "STEP " + i, desc, 0, null, null, img, null
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

		String[] lines = partsDetails.split("\\r?\\n");
		List<RecipeIngredient> domains = new ArrayList<>();
		int sortOrder = 1;

		for (String line : lines) {
			if (line == null) continue;
			line = line.trim();
			if (line.isBlank()) continue;

			// 1) 완전 헤더 같은 라인은 날린다 (●, 재료, 소스, 곁들임채소 등)
			if (isHeaderLine(line)) {
				continue;
			}

			// 2) 콜론이 있으면 뒤쪽만 재료 부분으로 사용
			int colonIdx = line.indexOf(':');
			if (colonIdx >= 0 && colonIdx < line.length() - 1) {
				String rhs = line.substring(colonIdx + 1).trim();
				if (!rhs.isBlank()) {
					line = rhs;
				} else {
					// 콜론 뒤에 아무것도 없으면 그냥 스킵
					continue;
				}
			}

			// 3) 한 줄 안에서도 콤마로 여러 재료가 있을 수 있음
			String[] pieces = splitPartsSafe(line);
			for (String piece : pieces) {
				String token = piece.trim();
				if (token.isBlank()) continue;

				// 숫자/분수/약간/적당량 하나도 없으면 재료로 보기 애매 → 스킵
				if (!looksLikeIngredientToken(token)) {
					continue;
				}

				ParsedIngredient parsed = parseIngredient(token);
				if (parsed == null) continue;

				Ingredient ingredient = ingredientAdapter.findLikeOrCreateIngredient(
					parsed.name(),
					IngredientCategoryEnum.UNKNOWN,
					0.0, 0.0, 0.0, 0.0
				);

				RecipeIngredient domain = RecipeIngredient.create(
					null,
					recipeId,
					ingredient,
					parsed.quantity(),
					parsed.unit(),
					parsed.note(),
					sortOrder++
				);
				domains.add(domain);
			}
		}

		ingredientAdapter.saveAllRecipeIngredients(domains);
	}

	private String[] splitPartsSafe(String raw) {
		List<String> result = new ArrayList<>();
		StringBuilder current = new StringBuilder();
		int depth = 0;

		for (int i = 0; i < raw.length(); i++) {
			char c = raw.charAt(i);

			if (c == '(') {
				depth++;
				current.append(c);
			} else if (c == ')') {
				if (depth > 0) depth--;
				current.append(c);
			} else if ((c == ',' || c == '\n' || c == '\r') && depth == 0) {
				String token = current.toString().trim();
				if (!token.isEmpty()) {
					result.add(token);
				}
				current.setLength(0);
			} else {
				current.append(c);
			}
		}

		String last = current.toString().trim();
		if (!last.isEmpty()) {
			result.add(last);
		}

		return result.toArray(new String[0]);
	}

	private boolean isHeaderLine(String line) {
		// ●, :, 재료/양념/소스/곁들임채소 등이 있고 숫자는 거의 없는 라인 → 헤더로 간주
		String cleaned = line.replaceAll("\\s+", "");
		boolean hasDigitOrFrac = cleaned.matches(".*[0-9⅓⅔¼½¾⅛⅜⅝⅞].*");

		if (!hasDigitOrFrac &&
			(cleaned.contains("●")
				|| cleaned.contains("재료")
				|| cleaned.contains("양념")
				|| cleaned.contains("소스")
				|| cleaned.contains("곁들임")
				|| cleaned.endsWith(":"))) {
			return true;
		}
		return false;
	}

	private boolean looksLikeIngredientToken(String token) {
		String cleaned = token.replaceAll("\\s+", "");
		boolean hasDigitOrFrac = cleaned.matches(".*[0-9⅓⅔¼½¾⅛⅜⅝⅞].*");
		boolean hasApproxWord =
			token.contains("약간") || token.contains("적당량") || token.contains("알맞게");

		return hasDigitOrFrac || hasApproxWord;
	}

	// 수량 + 단위 (정수, 소수, 분수, 유니코드 분수 포함)
	// 예: "10g", "7 g", "1/2컵", "1⅓작은술", "½컵"
	private static final Pattern QTY_UNIT_PATTERN = Pattern.compile(
		//"([0-9]+(?:/[0-9]+)?(?:\\.[0-9]+)?|[0-9]*[⅓⅔½¼¾⅛⅜⅝⅞])\\s*([a-zA-Z가-힣°]+)"
		"([0-9]+(?:/[0-9]+)?(?:\\.[0-9]+)?|[0-9]*[⅓⅔½¼¾⅛⅜⅝⅞])\\s*([a-zA-Z가-힣°㎖㎜㎝㎏㎎㎥㎔㎞㎚]+)"
	);

	// 그룹 라벨 prefix (재료, 소스, 양념 등)
	private static final String[] GROUP_PREFIXES = {
		"재료", "주재료", "부재료", "양념", "소스", "고명", "곁들임채소", "곁들임 채소"
	};

	// 전처리/상태 키워드 -> note 로 빼기 (name 앞에 붙어 있는 경우)
	private static final String[] NOTE_KEYWORDS = {
		"다진", "채썬", "썬", "곱게 다진", "잘게 다진",
		"불린", "데친", "삶은", "볶은", "찐", "구운", "말린"
	};

	private static final Set<String> GENERIC_PREFIXES = Set.of(
		"재료", "불린", "삶은", "데친", "다진", "볶은", "구운"
	);

	private static final Pattern BRACKET_PATTERN = Pattern.compile("\\((.*?)\\)");
	// 숫자 + 단위: "7g", "1.5컵", "1/2컵", "1⅓작은술" 등

	private static final Map<Character, Double> FRACTION_MAP = Map.ofEntries(
		Map.entry('¼', 1.0 / 4),
		Map.entry('½', 1.0 / 2),
		Map.entry('¾', 3.0 / 4),
		Map.entry('⅓', 1.0 / 3),
		Map.entry('⅔', 2.0 / 3),
		Map.entry('⅛', 1.0 / 8),
		Map.entry('⅜', 3.0 / 8),
		Map.entry('⅝', 5.0 / 8),
		Map.entry('⅞', 7.0 / 8)
	);


	public ParsedIngredient parseIngredient(String rawText) {
		if (rawText == null) return null;

		String text = rawText.trim();
		if (text.isEmpty()) return null;

		// [1인분] 같은 헤더 제거
		text = text.replaceAll("^\\[[^\\]]*\\]", "").trim();
		if (text.isEmpty()) return null;

		String note = null;
		String base = text;

		// 1) 괄호 내용 분리
		//    "오리고기(훈제오리 가슴살, 150g)" ->
		//       base = "오리고기"
		//       parenContent = "훈제오리 가슴살, 150g"
		String parenContent = null;
		Matcher parenMatcher = BRACKET_PATTERN.matcher(text);
		if (parenMatcher.find()) {
			parenContent = parenMatcher.group(1).trim();
			base = text.substring(0, parenMatcher.start()).trim();
		}

		// 2) 전처리 키워드 NOTE 처리
		for (String keyword : NOTE_KEYWORDS) {
			if (base.startsWith(keyword)) {
				String remaining = base.substring(keyword.length()).trim();
				note = joinNotes(note, keyword);
				base = remaining;
				break;
			}
		}

		// 3) base 에서 숫자 + 단위 찾기
		//    ex) "연두부 75g" -> name: 연두부, qty: 75, unit: g
		Matcher m = QTY_UNIT_PATTERN.matcher(base);

		String name;
		double quantity;
		RecipeIngredientUnitEnum unit;

		if (m.find()) {
			// 3-1) base 자체에 수량이 있는 케이스
			String quantityStr = m.group(1);
			String unitStr = m.group(2);

			name = base.substring(0, m.start()).trim();
			quantity = parseQuantityNumber(quantityStr);
			unit = IngredientMapper.mapUnit(unitStr);

			// 괄호 안 내용은 note로만 활용
			if (parenContent != null && !parenContent.isBlank()) {
				note = joinNotes(note, parenContent);
			}

			if (name.isEmpty() && parenContent != null) {
				// 엣지 케이스: 괄호밖은 비어 있고, 괄호 안에 이름/수량이 다 들어간 경우
				return parseNoNumberCase(base, parenContent, note);
			}
		} else {
			// 3-2) base 에는 숫자가 없고, 괄호 안에만 수량이 있는 케이스 등
			if (parenContent != null && !parenContent.isBlank()) {
				Matcher m2 = QTY_UNIT_PATTERN.matcher(parenContent);
				if (m2.find()) {
					// 괄호 안에서 수량 + 단위 추출
					String quantityStr = m2.group(1);
					String unitStr = m2.group(2);

					name = base.trim();
					quantity = parseQuantityNumber(quantityStr);
					unit = IngredientMapper.mapUnit(unitStr);

					// 예: "오리고기(훈제오리 가슴살, 150g)"
					String before2 = parenContent.substring(0, m2.start())
						.replaceAll("[,\\s]+$", "")
						.trim();
					String after2 = parenContent.substring(m2.end())
						.replaceAll("^[,\\s]+", "")
						.trim();

					if (before2.isEmpty() && after2.isEmpty()) {
						// "1⅓작은술" 처럼 수량+단위만 있는 경우
						note = joinNotes(note, parenContent);
					} else {
						note = joinNotes(note, before2); // "훈제오리 가슴살"
						note = joinNotes(note, after2);
					}

					// "재료 느타리버섯(10g)" 같은 케이스 처리:
					// base 가 "재료 느타리버섯" 이면, 이름을 뒷부분으로 정교하게 자르기
					name = normalizeGenericPrefixName(name);

				} else {
					// 괄호 안에도 수량이 없으면, 통째로 이름/노트로 처리
					return parseNoNumberCase(base, parenContent, note);
				}
			} else {
				// 숫자 패턴이 전혀 없는 경우
				return parseNoNumberCase(base, null, note);
			}
		}

		if (note == null) note = "";
		return new ParsedIngredient(name, quantity, unit, note);
	}

	/**
	 * base 에 숫자가 없고, 괄호에도 숫자가 없거나 수량 패턴을 못 찾은 경우 처리.
	 */
	private ParsedIngredient parseNoNumberCase(String base, String parenContent, String note) {
		String text = base.trim();
		if (text.isEmpty()) return null;

		// "재료 느타리버섯" 같은 구문에서 "재료" 제거
		String name = normalizeGenericPrefixName(text);

		// note 에 괄호 내용 추가
		if (parenContent != null && !parenContent.isBlank()) {
			note = joinNotes(note, parenContent.trim());
		}

		if (note == null) note = "";

		// 수량/단위 불명 -> 기본값
		return new ParsedIngredient(
			name,
			1.0,
			RecipeIngredientUnitEnum.TO_TASTE,
			note
		);
	}

	/**
	 * "재료 느타리버섯" → "느타리버섯"
	 * "불린 당면"      → "당면"
	 */
	private String normalizeGenericPrefixName(String text) {
		String[] tokens = text.split("\\s+");
		if (tokens.length >= 2 && GENERIC_PREFIXES.contains(tokens[0])) {
			// 첫 토큰이 일반 접두사면, 나머지를 이름으로 사용
			return String.join(" ", Arrays.copyOfRange(tokens, 1, tokens.length)).trim();
		}
		return text;
	}

	/**
	 * note 병합 유틸.
	 */
	private String joinNotes(String base, String extra) {
		if (extra == null || extra.isBlank()) return base;
		if (base == null || base.isBlank()) return extra.trim();
		return base.trim() + ", " + extra.trim();
	}

	/**
	 * "1", "1.5", "1/2", "3/4", "1 1/2", "1⅓" 등 문자열을 double 로 변환
	 */
	private double parseQuantityNumber(String raw) {
		if (raw == null) return 1.0;
		String s = raw.trim();
		if (s.isEmpty()) return 1.0;

		// "1½" → "1 ½" 처럼 정수와 분수 사이에 공백 삽입
		s = insertSpaceBeforeFractionChar(s);

		// 유니코드 분수 문자를 "1/2" 같은 텍스트로 치환
		s = normalizeFractionText(s);

		// "1 1/2" 같은 혼합 분수 처리
		if (s.contains(" ")) {
			String[] parts = s.split("\\s+");
			double total = 0.0;
			for (String part : parts) {
				total += parseSingleNumber(part);
			}
			return total;
		} else {
			return parseSingleNumber(s);
		}
	}

	/**
	 * 단일 숫자 토큰 파싱: "1", "1.5", "1/2" 등
	 */
	private double parseSingleNumber(String token) {
		String t = token.trim();
		if (t.isEmpty()) return 1.0;

		// "1/2" 같은 분수
		if (t.matches("\\d+\\/\\d+")) {
			String[] arr = t.split("/");
			double num = Double.parseDouble(arr[0]);
			double den = Double.parseDouble(arr[1]);
			if (den == 0) return 0.0;
			return num / den;
		}

		try {
			return Double.parseDouble(t);
		} catch (NumberFormatException e) {
			// 파싱 실패 시 기본값
			return 1.0;
		}
	}

	/**
	 * "1½" → "1 ½" 처럼 정수와 유니코드 분수 사이에 공백을 넣음.
	 */
	private String insertSpaceBeforeFractionChar(String s) {
		StringBuilder sb = new StringBuilder();
		char prev = 0;
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (FRACTION_MAP.containsKey(c) && Character.isDigit(prev)) {
				sb.append(' ');
			}
			sb.append(c);
			prev = c;
		}
		return sb.toString();
	}

	/**
	 * 유니코드 분수를 "1/2" 같은 형태로 치환
	 */
	private String normalizeFractionText(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			Double v = FRACTION_MAP.get(c);
			if (v != null) {
				// 유니코드 분수를 "a/b" 형태로 치환
				// 여기선 실제 분수값 대신 "1/2" 같은 텍스트로 통일
				// 하지만 이미 insertSpaceBeforeFractionChar 로 공백이 들어가 있으므로
				// "1 1/2" 형태가 됨.
				if (c == '¼') sb.append("1/4");
				else if (c == '½') sb.append("1/2");
				else if (c == '¾') sb.append("3/4");
				else if (c == '⅓') sb.append("1/3");
				else if (c == '⅔') sb.append("2/3");
				else if (c == '⅛') sb.append("1/8");
				else if (c == '⅜') sb.append("3/8");
				else if (c == '⅝') sb.append("5/8");
				else if (c == '⅞') sb.append("7/8");
				else sb.append(c); // fallback
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

}
