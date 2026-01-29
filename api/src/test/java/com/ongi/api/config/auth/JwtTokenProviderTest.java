package com.ongi.api.config.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.ongi.api.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private JwtProperties props;

    // HS256 requires at least 256 bits (32 bytes)
    private final String accessKey = "this-is-a-32-byte-access-key-!!"; // 31 bytes -> one more
    private final String validAccessKey = "this-is-a-32-byte-access-key-123"; // 32 bytes
    private final String validRefreshKey = "this-is-a-32-byte-refresh-key-12"; // 32 bytes
    private final String issuer = "test-issuer";

    @BeforeEach
    void setUp() {
        props = new JwtProperties(
            issuer,
            3600L,
            86400L,
            validAccessKey,
            validRefreshKey
        );
        jwtTokenProvider = new JwtTokenProvider(props);
    }

    @Test
    @DisplayName("액세스 토큰 생성 및 파싱 - 성공")
    void createAndParseAccessToken_success() {
        // given
        String userId = "123";
        Map<String, Object> claims = Map.of("roles", List.of("ROLE_USER"));

        // when
        String token = jwtTokenProvider.createAccessToken(userId, claims);
        Jws<Claims> jws = jwtTokenProvider.parseAccess(token);

        // then
        assertThat(jws.getBody().getSubject()).isEqualTo(userId);
        assertThat(jws.getBody().getIssuer()).isEqualTo(issuer);
        assertThat(jws.getBody().get("typ")).isEqualTo("ACCESS");
    }

    @Test
    @DisplayName("잘못된 서명의 토큰 파싱 - 실패")
    void parseToken_invalidSignature_fail() {
        // given
        JwtProperties otherProps = new JwtProperties(
            issuer, 3600L, 86400L,
            "another-32-byte-secret-key-123456", // 32 bytes
            validRefreshKey
        );
        JwtTokenProvider otherProvider = new JwtTokenProvider(otherProps);
        String invalidToken = otherProvider.createAccessToken("123", Map.of());

        // when & then
        assertThrows(SignatureException.class, () -> {
            jwtTokenProvider.parseAccess(invalidToken);
        });
    }

    @Test
    @DisplayName("만료된 토큰 파싱 - 실패")
    void parseToken_expired_fail() {
        // given
        JwtProperties expiredProps = new JwtProperties(
            issuer, -10L, 0L, validAccessKey, validRefreshKey
        );
        JwtTokenProvider expiredProvider = new JwtTokenProvider(expiredProps);
        String token = expiredProvider.createAccessToken("123", Map.of());

        // when & then
        assertThrows(JwtException.class, () -> {
            jwtTokenProvider.parseAccess(token);
        });
    }

    @Test
    @DisplayName("토큰 타입 불일치 (Access 파서로 Refresh 토큰 파싱) - 실패")
    void parseToken_typeMismatch_fail() {
        // given
        String refreshToken = jwtTokenProvider.createRefreshToken("123", "jti");

        // when & then
        // AccessParser uses validAccessKey, while RefreshToken is signed with validRefreshKey
        assertThrows(SignatureException.class, () -> {
            jwtTokenProvider.parseAccess(refreshToken);
        });
    }

    @Test
    @DisplayName("권한(roles) 추출 확인")
    void getRolesFromToken_success() {
        // given
        String userId = "123";
        List<String> roles = List.of("ROLE_USER", "ROLE_ADMIN");
        String token = jwtTokenProvider.createAccessToken(userId, Map.of("roles", roles));

        // when
        List<String> extractedRoles = jwtTokenProvider.getRolesFromToken(token);

        // then
        assertThat(extractedRoles).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
