package org.app.auth.domain.token;

import io.jsonwebtoken.*;
import javax.crypto.*;

@SuppressWarnings("ClassCanBeRecord")
public class JwtAuthenticator {

    private final String jwtIssuer;
    private final SecretKey secretKey;

    protected JwtAuthenticator(String jwtIssuer, SecretKey secretKey) {
        this.jwtIssuer = jwtIssuer;
        this.secretKey = secretKey;
    }

    public CustomJwtPayloadClaims getPayloadClaimsFromToken(String token) {
        Claims payloadClaims = Jwts.parser()
                .verifyWith(secretKey).requireIssuer(jwtIssuer).build()
                .parseSignedClaims(token)
                .getPayload();

        return new CustomJwtPayloadClaims(payloadClaims);
    }
}
