package org.app.auth.domain.token;

import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class RefreshTokenManager extends AbstractTokenManager {

    private static final String RT_ISS = "Beat the AI - RT";

    protected RefreshTokenManager(
            @Value("${jwt.refresh-token.signature}")
            String signature,
            @Value("${jwt.refresh-token.expiration}")
            long expiration
    ) {
        super(RT_ISS, signature, expiration);
    }
}
