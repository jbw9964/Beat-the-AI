package org.app.auth.domain.token;

import io.jsonwebtoken.*;
import java.util.*;
import javax.crypto.*;
import lombok.*;
import org.app.util.*;

@SuppressWarnings("ClassCanBeRecord")
public class JwtProvider {

    private final String jwtIssuer;
    private final SecretKey secretKey;

    @Getter(AccessLevel.PUBLIC)
    private final long expiration;

    public JwtProvider(String jwtIssuer, SecretKey secretKey, long expiration) {
        this.jwtIssuer = jwtIssuer;
        this.secretKey = secretKey;
        this.expiration = expiration;
    }

    public final String create(
            @NonNull CustomJwtPayloadClaims customPayloadClaims
    ) {

        Date iat = DateTimeProvider.dateNow();
        Date exp = DateTimeProvider.dateNowAfter(expiration);

        Claims claims = customPayloadClaims.buildCustomClaims()
                .issuer(jwtIssuer)
                .issuedAt(iat)
                .expiration(exp)
                .build();

        return Jwts.builder()
                .claims(claims)
                .signWith(secretKey)
                .compact();
    }
}
