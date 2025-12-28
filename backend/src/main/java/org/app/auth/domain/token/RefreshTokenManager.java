package org.app.auth.domain.token;

import org.app.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class RefreshTokenManager extends AbstractTokenManager {

    private static final String RT_ISS = "Beat the AI - RT";

    protected RefreshTokenManager(
            @Value("${jwt.rt.sign}")
            String signature,
            @Value("${jwt.rt.exp}")
            long expiration,
            DateTimeProvider dateTimeProvider
    ) {
        super(RT_ISS, signature, expiration, dateTimeProvider);
    }
}
