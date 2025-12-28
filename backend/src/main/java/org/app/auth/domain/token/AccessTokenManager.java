package org.app.auth.domain.token;

import org.app.util.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.*;

@Component
public class AccessTokenManager extends AbstractTokenManager {

    private static final String AT_ISS = "Beat the AI - AT";

    protected AccessTokenManager(
            @Value("${jwt.at.sign}")
            String signature,
            @Value("${jwt.at.exp}")
            long expiration,
            DateTimeProvider dateTimeProvider
    ) {
        super(AT_ISS, signature, expiration, dateTimeProvider);
    }
}
