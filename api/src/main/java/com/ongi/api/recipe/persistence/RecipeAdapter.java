package com.ongi.api.recipe.persistence;

import com.ongi.api.ingredients.persistence.QRecipeIngredientEntity;
import com.ongi.api.recipe.persistence.repository.RecipeCommentRepository;
import com.ongi.api.recipe.persistence.repository.RecipeLikeRepository;
import com.ongi.api.recipe.persistence.repository.RecipeRepository;
import com.ongi.api.recipe.persistence.repository.RecipeStatsRepository;
import com.ongi.api.recipe.persistence.repository.RecipeStepsRepository;
import com.ongi.api.recipe.persistence.repository.RecipeTagsRepository;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeComment;
import com.ongi.recipe.domain.RecipeLike;
import com.ongi.recipe.domain.RecipeStats;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.RecipeTags;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import com.ongi.recipe.port.RecipeRepositoryPort;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
// TODO Port 이거저거 너무 많이 들어있어서 사이즈가 너무 큼. 각 UseCase 별로 분리
public class RecipeAdapter implements RecipeRepositoryPort {

	private final JPAQueryFactory queryFactory;

	private final RecipeRepository recipeRepository;

	private final RecipeStepsRepository recipeStepsRepository;

	private final RecipeTagsRepository recipeTagsRepository;

	private final RecipeLikeRepository recipeLikeRepository;

	private final RecipeStatsRepository recipeStatsRepository;

	private final RecipeCommentRepository recipeCommentRepository;

	@Override
	public Recipe save(Recipe recipe) {
		RecipeEntity entity = RecipeMapper.toEntity(recipe);
		RecipeEntity saved = recipeRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public Optional<Recipe> findRecipeById(Long id) {
		return recipeRepository
			.findById(id)
			.map(RecipeMapper::toDomain);
	}

	@Override
	public boolean existsRecipeById(Long id) {
		return recipeRepository.existsById(id);
	}

	@Override
	public void deleteRecipeById(Long id) {
		recipeRepository.deleteById(id);
	}

	@Override
	public RecipeSteps save(RecipeSteps recipeSteps) {
		RecipeStepsEntity entity = RecipeMapper.toEntity(recipeSteps);
		RecipeStepsEntity saved = recipeStepsRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public List<RecipeSteps> saveAllRecipeSteps(List<RecipeSteps> recipeSteps) {
		List<RecipeStepsEntity> entities = recipeSteps.stream().map(RecipeMapper::toEntity).toList();
		List<RecipeStepsEntity> saved = recipeStepsRepository.saveAll(entities);
		return saved.stream().map(RecipeMapper::toDomain).toList();
	}

	@Override
	public Optional<RecipeSteps> findRecipeStepsById(Long id) {
		return recipeStepsRepository
			.findById(id)
			.map(RecipeMapper::toDomain);
	}

	@Override
	public List<RecipeSteps> findRecipeStepsByRecipeId(Long id) {
		return recipeStepsRepository
			.findRecipeStepsByRecipeId(id)
			.stream()
			.map(RecipeMapper::toDomain)
			.toList();
	}

	@Override
	public void deleteRecipeStepsByRecipeId(Long recipeId) {
		recipeStepsRepository.deleteById(recipeId);
	}

	@Override
	public RecipeTags save(RecipeTags recipeTags) {
		RecipeTagsEntity entity = RecipeMapper.toEntity(recipeTags);
		RecipeTagsEntity saved = recipeTagsRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public List<RecipeTags> saveAllRecipeTags(List<RecipeTags> recipeTags) {
		List<RecipeTagsEntity> entities = recipeTags.stream().map(RecipeMapper::toEntity).toList();
		List<RecipeTagsEntity> saved = recipeTagsRepository.saveAll(entities);
		return saved.stream().map(RecipeMapper::toDomain).toList();
	}

	@Override
	public Optional<RecipeTags> findRecipeTagsById(Long id) {
		return recipeTagsRepository
			.findById(id)
			.map(RecipeMapper::toDomain);
	}

	@Override
	public List<Recipe> search(RecipeSearchCondition condition, Long cursor, Integer size, PageSortOptionEnum sort) {
		QRecipeEntity recipe = QRecipeEntity.recipeEntity;
		QRecipeTagsEntity recipeTags = QRecipeTagsEntity.recipeTagsEntity;
		QRecipeIngredientEntity recipeIngredients = QRecipeIngredientEntity.recipeIngredientEntity;
		// QIngredientEntity ingredients = QIngredientEntity.ingredientEntity;

		JPAQuery<RecipeEntity> query = queryFactory
			.selectFrom(recipe)
			.distinct();

		BooleanBuilder where = new BooleanBuilder();

		// ====== 1) 검색 조건 ======
		// 키워드
		if (condition.getKeyword() != null && !condition.getKeyword().isBlank()) {
			String kw = condition.getKeyword().trim();
			where.and(
				recipe.title.containsIgnoreCase(kw)
			);
		}

		// 태그
		if (condition.getTag() != null && !condition.getTag().isBlank()) {
			query.leftJoin(recipeTags).on(recipeTags.recipeId.eq(recipe.id));
			where.and(recipeTags.tag.eq(condition.getTag().trim()));
		}

		// 카테고리
		if (condition.getCategory() != null) {
			where.and(recipe.category.eq(condition.getCategory()));
		}

		// 특정 재료 포함 레시피
		if (condition.getIngredientId() != null) {
			query.leftJoin(recipeIngredients).on(recipeIngredients.recipeId.eq(recipe.id));
			// TODO 확인 필요 + 재료 이름으로 할 수도 있음.
			//query.leftJoin(ingredients).on(recipeIngredients.ingredient.eq(ingredients));
			where.and(recipeIngredients.ingredient.id.eq(condition.getIngredientId()));
		}

		// 최대 조리 시간
		if (condition.getMaxCookingTimeMin() != null) {
			where.and(recipe.cookingTimeMin.loe(condition.getMaxCookingTimeMin()));
		}

		// ====== 2) 커서 조건 ======
		if (cursor != null) {
			switch (sort) {
				case CREATED_ASC, ID_ASC, VIEWS_ASC -> {
					// ASC 계열 → cursor 보다 큰 id
					where.and(recipe.id.gt(cursor));
				}
				case CREATED_DESC, ID_DESC, VIEWS_DESC -> {
					// DESC 계열 → cursor 보다 작은 id
					where.and(recipe.id.lt(cursor));
				}
			}
		}

		query.where(where);

		// ====== 3) 정렬 ======
		OrderSpecifier<?>[] orderSpecifiers = toOrderSpecifiers(sort, recipe);
		query.orderBy(orderSpecifiers);

		// ====== 4) limit ======
		int pageSize = (size == null || size <= 0) ? 20 : Math.min(size, 100);
		query.limit(pageSize);

		// ====== 5) 실행 & 매핑 ======
		List<RecipeEntity> entities = query.fetch();

		return entities.stream()
			.map(RecipeMapper::toDomain)
			.toList();
	}

	@Override
	public RecipeLike save(RecipeLike recipeLike) {
		RecipeLikeEntity entity = RecipeMapper.toEntity(recipeLike);
		RecipeLikeEntity saved = recipeLikeRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public Optional<RecipeLike> findRecipeLikeByRecipeIdAndUserId(Long recipeId, Long userId) {
		return recipeLikeRepository
			.findById(new RecipeLikeId(recipeId, userId))
			.map(RecipeMapper::toDomain);
	}

	@Override
	public void deleteRecipeLikeByRecipeIdAndUserId(Long recipeId, Long userId) {
		recipeLikeRepository.deleteById(new RecipeLikeId(recipeId, userId));
	}

	@Override
	public RecipeStats save(RecipeStats domain) {
		RecipeStatsEntity entity = RecipeMapper.toEntity(domain);
		RecipeStatsEntity saved = recipeStatsRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public Optional<RecipeStats> findRecipeStatsByRecipeId(Long recipeId) {
		return recipeStatsRepository
			.findById(recipeId)
			.map(RecipeMapper::toDomain);
	}

	@Override
	public List<RecipeStats> findRecipeStatsByRecipeIds(List<Long> recipeIds) {
		return recipeStatsRepository.findAllById(recipeIds)
			.stream()
			.map(RecipeMapper::toDomain)
			.toList();
	}

	@Override
	public RecipeComment save(RecipeComment domain) {
		RecipeCommentEntity entity = RecipeMapper.toEntity(domain);
		RecipeCommentEntity saved = recipeCommentRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public boolean existsRecipeCommentById(Long id) {
		return recipeCommentRepository.existsById(id);
	}

	@Override
	public Optional<RecipeComment> findRecipeCommentByIdAndRecipeId(Long id,
		Long recipeId) {
		return recipeCommentRepository
			.findByIdAndRecipeId(id, recipeId)
			.map(RecipeMapper::toDomain);
	}

	@Override
	public Optional<RecipeComment> findRecipeCommentByIdAndRecipeIdAndStatus(Long id, Long recipeId,
		RecipeCommentStatus status) {
		return recipeCommentRepository
			.findByIdAndRecipeIdAndStatus(id, recipeId, status)
			.map(RecipeMapper::toDomain);
	}

	@Override
	public RecipeComment createRootComment(Long recipeId, Long userId, String content) {
		RecipeCommentEntity entity = RecipeCommentEntity.createRoot(recipeId, userId, content);
		RecipeCommentEntity saved = recipeCommentRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public RecipeComment createReplyComment(Long recipeId, Long userId, String content, Long parentId) {
		RecipeCommentEntity entity = RecipeCommentEntity.createReply(recipeId, userId, content, parentId);
		RecipeCommentEntity saved = recipeCommentRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public RecipeComment updateRecipeCommentContent(RecipeComment domain, String content) {
		RecipeCommentEntity entity = RecipeMapper.toEntity(domain);
		entity.updateContent(content);
		RecipeCommentEntity saved = recipeCommentRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public boolean deleteRecipeCommentSoft(RecipeComment domain) {
		RecipeCommentEntity entity = RecipeMapper.toEntity(domain);
		return entity.deleteSoft();
	}


	private OrderSpecifier<?>[] toOrderSpecifiers(PageSortOptionEnum sort, QRecipeEntity recipe) {
		return switch (sort) {
			case CREATED_ASC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.ASC, recipe.createdAt),
				new OrderSpecifier<>(Order.ASC, recipe.id)
			};
			case CREATED_DESC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.DESC, recipe.createdAt),
				new OrderSpecifier<>(Order.DESC, recipe.id)
			};
			case ID_ASC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.ASC, recipe.id)
			};
			case ID_DESC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.DESC, recipe.id)
			};

			// TODO View 관련 추가 필요.
			case VIEWS_ASC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.ASC, recipe.id),
			};
			case VIEWS_DESC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.DESC, recipe.id),
			};
			/*case VIEWS_ASC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.ASC, recipe.views),
				new OrderSpecifier<>(Order.ASC, recipe.id)
			};
			case VIEWS_DESC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.DESC, recipe.views),
				new OrderSpecifier<>(Order.DESC, recipe.id)
			};*/
		};
	}


}
