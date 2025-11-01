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
    private final DateTimeProvider dtProvider;

    public JwtProvider(
            String jwtIssuer, SecretKey secretKey, long expiration,
            DateTimeProvider dtProvider
    ) {
        this.jwtIssuer = jwtIssuer;
        this.secretKey = secretKey;
        this.expiration = expiration;
        this.dtProvider = dtProvider;
    }

    public final String create(
            @NonNull CustomJwtPayloadClaims customPayloadClaims
    ) {

        Date iat = dtProvider.dateNow();
        Date exp = dtProvider.secAfterFromDate(iat, expiration);

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
