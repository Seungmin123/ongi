package com.ongi.api.user.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ongi.api.common.web.dto.JwtTokens;
import com.ongi.api.config.auth.JwtTokenProvider;
import com.ongi.api.config.properties.JwtProperties;
import com.ongi.api.user.adapter.out.cache.UserRedisTemplate;
import com.ongi.api.user.adapter.out.cache.store.EmailVerificationStore;
import com.ongi.api.user.adapter.out.cache.store.PasswordResetTokenStore;
import com.ongi.api.user.adapter.out.cache.store.RefreshTokenStore;
import com.ongi.api.user.adapter.out.cache.store.SignUpTokenStore;
import com.ongi.api.user.adapter.out.persistence.UserAdapter;
import com.ongi.api.user.application.component.MailSender;
import com.ongi.api.user.web.dto.EmailVerifyConfirmRequest;
import com.ongi.api.user.web.dto.EmailVerifyRequest;
import com.ongi.api.user.web.dto.MemberLoginRequest;
import com.ongi.api.user.web.dto.MemberSignUpRequest;
import com.ongi.api.user.web.dto.SignUpTokenResponse;
import com.ongi.user.domain.User;
import com.ongi.user.domain.enums.UserTier;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private FileService fileService;
    @Mock private UserAdapter userAdapter;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailVerificationStore emailVerificationStore;
    @Mock private SignUpTokenStore signUpTokenStore;
    @Mock private RefreshTokenStore refreshStore;
    @Mock private PasswordResetTokenStore passwordResetTokenStore;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private JwtProperties props;
    @Mock private MailSender mailSender;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("이메일 인증 확인 - 성공")
    void confirmEmailVerification_success() {
        String email = "test@example.com";
        String code = "123456";
        String codeHash = UserRedisTemplate.sha256(code);
        
        EmailVerifyConfirmRequest req = new EmailVerifyConfirmRequest(email, code);
        given(userAdapter.existsUserByEmail(email)).willReturn(false);
        given(emailVerificationStore.getCodeHash(email)).willReturn(codeHash);
        given(emailVerificationStore.incrAttempt(email)).willReturn(1L);

        SignUpTokenResponse result = authService.confirmEmailVerification(req);

        assertThat(result.signUpToken()).isNotNull();
        verify(emailVerificationStore).clear(email);
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signUp_success() {
        MemberSignUpRequest req = new MemberSignUpRequest("test@example.com", "pass1234", "nick", UUID.randomUUID(), "tmp/key.jpg", "signup_token");
        given(signUpTokenStore.getEmail(anyString())).willReturn(req.email());
        given(userAdapter.existsUserByEmail(req.email())).willReturn(false);
        given(passwordEncoder.encode(any())).willReturn("encoded_pass");
        
        User savedUser = mock(User.class);
        given(savedUser.getId()).willReturn(1L);
        given(userAdapter.save(any(User.class))).willReturn(savedUser);

        authService.signUp(req);

        verify(signUpTokenStore).consume(anyString());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {
        MemberLoginRequest req = new MemberLoginRequest("test@example.com", "pass1234");
        User user = mock(User.class);
        given(user.getId()).willReturn(1L);
        given(user.getPasswordHash()).willReturn("encoded_pass");
        given(user.getTier()).willReturn(UserTier.USER);
        
        given(userAdapter.findUserByEmail(req.email())).willReturn(Optional.of(user));
        given(passwordEncoder.matches(req.password(), "encoded_pass")).willReturn(true);
        given(jwtTokenProvider.createAccessToken(anyString(), any())).willReturn("access");
        given(jwtTokenProvider.createRefreshToken(anyString(), anyString())).willReturn("refresh");
        given(props.refreshExpSeconds()).willReturn(3600L);

        JwtTokens result = authService.login(req);

        assertThat(result.accessToken()).isEqualTo("access");
    }
}
