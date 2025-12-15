package com.ongi.api.config.auth;

import com.ongi.api.common.web.dto.JwtTokens;
import com.ongi.api.config.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JwtTokenProvider {

    public enum TokenType { ACCESS, REFRESH }

    private final JwtProperties props;

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    private final JwtParser accessParser;
    private final JwtParser refreshParser;

    public JwtTokenProvider(JwtProperties props) {
        this.props = props;

        this.accessKey = Keys.hmacShaKeyFor(props.accessKey().getBytes(StandardCharsets.UTF_8));
        this.refreshKey = Keys.hmacShaKeyFor(props.refreshKey().getBytes(StandardCharsets.UTF_8));

        this.accessParser = Jwts.parserBuilder()
            .setSigningKey(accessKey)
            .requireIssuer(props.issuer())
            .build();

        this.refreshParser = Jwts.parserBuilder()
            .setSigningKey(refreshKey)
            .requireIssuer(props.issuer())
            .build();
    }

    public String createAccessToken(String userId, Map<String, Object> claims) {
        return createToken(userId, TokenType.ACCESS, props.accessExpSeconds(), claims, accessKey, null);
    }

    public String createRefreshToken(String userId, String jti) {
        return createToken(userId, TokenType.REFRESH, props.refreshExpSeconds(), Map.of(), refreshKey, jti);
    }

    private String createToken(
        String userId,
        TokenType type,
        long expSeconds,
        Map<String, Object> claims,
        Key signingKey,
        String jti
    ) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expSeconds);

        JwtBuilder builder =  Jwts.builder()
            .setIssuer(props.issuer())
            .setSubject(userId)
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(exp))
            .claim("typ", type.name())
            .addClaims(claims)
            .signWith(signingKey);

        if(jti != null) builder.setId(jti);

        return builder.compact();
    }

    public Jws<Claims> parseAccess(String token) {
        Jws<Claims> jws = accessParser.parseClaimsJws(token);
        requireType(jws, TokenType.ACCESS);
        return jws;
    }

    // ✅ Refresh 전용 검증/파싱
    public Jws<Claims> parseRefresh(String token) {
        Jws<Claims> jws = refreshParser.parseClaimsJws(token);
        requireType(jws, TokenType.REFRESH);
        return jws;
    }

    private void requireType(Jws<Claims> jws, TokenType expected) {
        String typ = jws.getBody().get("typ", String.class);
        if (!expected.name().equals(typ)) {
            throw new IllegalArgumentException("Invalid token type: expected=" + expected + ", actual=" + typ);
        }
    }

    public String getSubject(String token) {
        return parseAccess(token).getBody().getSubject();
    }

    public TokenType getTokenType(String token) {
        String typ = parseAccess(token).getBody().get("typ", String.class);
        return TokenType.valueOf(typ);
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = parseAccess(token).getBody();

        Object raw = claims.get("roles");
        if (raw == null) {
            return List.of();
        }

        return ((List<?>) raw).stream()
            .map(String::valueOf)
            .toList();
    }

    public Collection<SimpleGrantedAuthority> toAuthoritiesFromAccessToken(String accessToken) {
        List<?> raw = parseAccess(accessToken).getBody().get("roles", List.class);
        if (raw == null) return List.of();

        return raw.stream()
            .map(String::valueOf)
            .map(SimpleGrantedAuthority::new)
            .toList();
    }

}
