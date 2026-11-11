package com.orrien.expense_tracker.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class JwtConfig {

    // HS256 = HMAC with SHA-256: the same secret key signs and verifies tokens
    public static final MacAlgorithm ALGORITHM = MacAlgorithm.HS256;

    private static final int MIN_KEY_BYTES = 32; // HS256 requires at least 256 bits

    @Bean
    public SecretKey jwtSecretKey(JwtProperties properties) {
        byte[] keyBytes;
        try {
            keyBytes = Base64.getDecoder().decode(properties.secret());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("jwt.secret must be Base64-encoded", e);
        }
        if (keyBytes.length < MIN_KEY_BYTES) {
            throw new IllegalStateException(
                    "jwt.secret is too short: " + keyBytes.length + " bytes, need at least " + MIN_KEY_BYTES);
        }
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // Signs new tokens. The decoder (verifying tokens) comes in the next task.
    @Bean
    public JwtEncoder jwtEncoder(SecretKey jwtSecretKey) {
        return NimbusJwtEncoder.withSecretKey(jwtSecretKey)
                .algorithm(ALGORITHM)
                .build();
    }
}
