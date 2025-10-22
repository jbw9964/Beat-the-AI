package org.app.auth.domain.token;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class AccessTokenManager extends AbstractTokenManager {

    private static final String AT_ISS = "Beat the AI - AT";

    protected AccessTokenManager(
            @Value("${jwt.access-token.signature}")
            String signature,
            @Value("${jwt.access-token.expiration}")
            long expiration
    ) {
        super(AT_ISS, signature, expiration);
    }
}
