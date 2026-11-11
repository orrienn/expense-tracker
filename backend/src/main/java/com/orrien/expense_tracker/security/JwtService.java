package com.orrien.expense_tracker.security;

import com.orrien.expense_tracker.config.JwtConfig;
import com.orrien.expense_tracker.config.JwtProperties;
import com.orrien.expense_tracker.model.User;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties properties;

    public JwtService(JwtEncoder jwtEncoder, JwtProperties properties) {
        this.jwtEncoder = jwtEncoder;
        this.properties = properties;
    }

    public Jwt generateToken(User user) {
        Instant now = Instant.now();

        // Claims = the token's payload. Anyone can read it (it's only Base64), so nothing secret here.
        JwtClaimsSet claims = JwtClaimsSet.builder()
                // "sub" (subject): who the token is about. The id doesn't change, unlike the email.
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(now.plus(properties.expiration()))
                .build();

        // The header must name the algorithm explicitly, otherwise the encoder assumes RS256
        JwsHeader header = JwsHeader.with(JwtConfig.ALGORITHM).build();

        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims));
    }
}
