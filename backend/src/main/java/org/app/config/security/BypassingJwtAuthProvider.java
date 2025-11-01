package org.app.config.security;

import lombok.*;
import lombok.extern.slf4j.*;
import org.app.config.security.dto.*;
import org.app.entity.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
@RequiredArgsConstructor
class BypassingJwtAuthProvider implements AuthenticationProvider {

    private final UserPrincipalProvider userPrincipalProvider;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        SimpleUserAuthentication authResult = null;

        if (authentication instanceof AccessTokenAuthentication(String accessToken)) {

            try {
                User user = userPrincipalProvider.findByAccessToken(accessToken);
                authResult = new SimpleUserAuthentication(user.getId());
            } catch (Exception e) {
                log.info("Failed to provide authentication: {}", e.getMessage(), e);
            }

        } else {
            log.warn("Incompatible authentication type: {}", authentication.getClass());
        }

        return authResult;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.isAssignableFrom(AccessTokenAuthentication.class);
    }
}
