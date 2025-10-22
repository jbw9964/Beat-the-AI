package org.app.auth.domain.token;

import io.jsonwebtoken.security.*;
import javax.crypto.*;
import lombok.*;
import org.app.entity.*;

public abstract class AbstractTokenManager {

    private final JwtProvider provider;
    private final JwtAuthenticator authenticator;

    protected AbstractTokenManager(String tokenIssuer, String signature, long expiration) {
        SecretKey key = Keys.hmacShaKeyFor(signature.getBytes());
        this.provider = new JwtProvider(tokenIssuer, key, expiration);
        this.authenticator = new JwtAuthenticator(tokenIssuer, key);
    }

    public String createTokenWith(@NonNull User user) {
        CustomJwtPayloadClaims customClaims = new CustomJwtPayloadClaims(user);
        return provider.create(customClaims);
    }

    public CustomJwtPayloadClaims getClaimsFrom(String token) {
        return authenticator.getPayloadClaimsFromToken(token);
    }
}
