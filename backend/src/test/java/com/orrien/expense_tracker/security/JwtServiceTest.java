package com.orrien.expense_tracker.security;

import com.orrien.expense_tracker.config.JwtConfig;
import com.orrien.expense_tracker.config.JwtProperties;
import com.orrien.expense_tracker.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Uses the real encoder (no mocks): we generate a token and decode it back to check its contents.
class JwtServiceTest {

    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-key-that-is-at-least-32-bytes".getBytes());

    private final JwtConfig jwtConfig = new JwtConfig();
    private final JwtProperties properties = new JwtProperties(SECRET, Duration.ofHours(1));
    private final SecretKey key = jwtConfig.jwtSecretKey(properties);
    private final JwtService jwtService = new JwtService(jwtConfig.jwtEncoder(key), properties);

    @Test
    void generateToken_containsUserIdAndExpiry() {
        Jwt token = jwtService.generateToken(userWithId(42L));

        Jwt decoded = decoderFor(key).decode(token.getTokenValue());

        assertThat(decoded.getSubject()).isEqualTo("42");
        assertThat(Duration.between(decoded.getIssuedAt(), decoded.getExpiresAt())).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void generateToken_isRejectedWhenVerifiedWithDifferentKey() {
        Jwt token = jwtService.generateToken(userWithId(42L));

        String otherSecret = Base64.getEncoder()
                .encodeToString("a-completely-different-key-also-32-bytes!".getBytes());
        SecretKey otherKey = jwtConfig.jwtSecretKey(new JwtProperties(otherSecret, Duration.ofHours(1)));

        // The signature doesn't match, so nobody without our secret can forge a token
        assertThatThrownBy(() -> decoderFor(otherKey).decode(token.getTokenValue()))
                .isInstanceOf(JwtException.class);
    }

    private static JwtDecoder decoderFor(SecretKey key) {
        return NimbusJwtDecoder.withSecretKey(key).macAlgorithm(JwtConfig.ALGORITHM).build();
    }

    // User has no setId() (the database assigns ids), so the test sets the field directly
    private static User userWithId(Long id) {
        User user = new User("jan@example.com", "hashed");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
