package org.app.auth.domain.token;

import io.jsonwebtoken.security.*;
import javax.crypto.*;
import lombok.*;
import org.app.entity.*;
import org.app.util.*;

public abstract class AbstractTokenManager {

    private final JwtProvider provider;
    private final JwtAuthenticator authenticator;

    protected AbstractTokenManager(
            String tokenIssuer, String signature, long expiration,
            DateTimeProvider dtProvider
    ) {
        SecretKey key = Keys.hmacShaKeyFor(signature.getBytes());
        this.provider = new JwtProvider(tokenIssuer, key, expiration, dtProvider);
        this.authenticator = new JwtAuthenticator(tokenIssuer, key);
    }

    public String createTokenWith(@NonNull User user) {
        CustomJwtPayloadClaims customClaims = new CustomJwtPayloadClaims(user);
        return provider.create(customClaims);
    }

    public CustomJwtPayloadClaims getClaimsFrom(String token) {
        return authenticator.getPayloadClaimsFromToken(token);
    }

    public final long getExpiration() {
        return provider.getExpiration();
    }
}
