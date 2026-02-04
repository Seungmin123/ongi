package com.ongi.api.user.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ongi.api.user.adapter.out.persistence.MyPageQueryAdapter;
import com.ongi.api.user.adapter.out.persistence.UserAdapter;
import com.ongi.api.user.port.IngredientsProvider;
import com.ongi.api.user.web.dto.MyPagePersonalizationUpdateRequest;
import com.ongi.api.user.web.dto.MyPageResponse;
import com.ongi.api.user.web.dto.MyPageSummaryUpdateRequest;
import com.ongi.ingredients.domain.AllergenGroup;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.user.domain.UserProfile;
import com.ongi.user.domain.enums.MeInclude;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private FileService fileService;
    @Mock private UserAdapter userAdapter;
    @Mock private MyPageQueryAdapter myPageQueryAdapter;
    @Mock private IngredientsProvider ingredientsProvider;
    @Mock private ObjectMapper objectMapper;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("마이페이지 요약 수정 - 성공")
    void myPageSummaryUpdate_success() {
        Long userId = 1L;
        MyPageSummaryUpdateRequest req = new MyPageSummaryUpdateRequest("newNick", java.util.UUID.randomUUID(), "tmp/key.jpg");
        UserProfile profile = mock(UserProfile.class);
        given(userAdapter.findUserProfileByUserId(userId)).willReturn(Optional.of(profile));
        given(fileService.consumeAndPromoteProfileImage(any(), anyString(), eq(userId))).willReturn("profile/1/img.jpg");

        userService.myPageSummaryUpdate(userId, req);

        verify(profile).setDisplayName("newNick");
        verify(userAdapter).save(profile);
    }

    @Test
    @DisplayName("개인화 정보 수정 - 존재하지 않는 알러지 그룹 ID 입력 시 실패")
    void myPagePersonalizationUpdate_unknown_allergen_fail() {
        // given
        Long userId = 1L;
        MyPagePersonalizationUpdateRequest req = new MyPagePersonalizationUpdateRequest(List.of(999L), 2000.0, List.of());
        
        // 1. dietGoal 업데이트 시 필요한 프로필 Mocking
        UserProfile profile = mock(UserProfile.class);
        given(userAdapter.findUserProfileByUserId(userId)).willReturn(Optional.of(profile));
        
        // 2. 알러지 그룹 검증 시 0개 반환하여 실패 유도
        given(ingredientsProvider.findAllergenGroupsByIds(any())).willReturn(Set.of());

        // when & then
        assertThatThrownBy(() -> userService.myPagePersonalizationUpdate(userId, req))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Unknown allergenGroupIds: [999]");
    }

    @Test
    @DisplayName("개인화 정보 수정 - 유저 프로필이 없는 경우 IllegalStateException")
    void myPagePersonalizationUpdate_user_not_found() {
        // given
        Long userId = 1L;
        MyPagePersonalizationUpdateRequest req = new MyPagePersonalizationUpdateRequest(List.of(), 2000.0, List.of());
        given(userAdapter.findUserProfileByUserId(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.myPagePersonalizationUpdate(userId, req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("User not found");
    }
}