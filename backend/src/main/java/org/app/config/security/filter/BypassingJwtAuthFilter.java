package org.app.config.security.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.config.security.domain.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.filter.*;

/**
 *
 */
@Slf4j
@RequiredArgsConstructor
public class BypassingJwtAuthFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        Optional<String> token = getBearerToken(request);

        if (token.isPresent()) {
            try {
                Authentication authResult = authenticationManager.authenticate(
                        new AccessTokenAuthentication(token.get())
                );

                SimpleUserAuthentication userAuth = (SimpleUserAuthentication) authResult;

                SecurityContextHolderStrategy holderStrategy = SecurityContextHolder.getContextHolderStrategy();
                SecurityContext newContext = holderStrategy.createEmptyContext();
                newContext.setAuthentication(userAuth);
                holderStrategy.setContext(newContext);

                log.info("JWT authentication has been set");

            } catch (Exception e) {
                log.info("Failed to authenticate: {}", e.getMessage(), e);
                log.info("Bypassing JWT Authentication");
            }
        } else {
            log.info("No JWT exists on Authorization header. Bypassing JWT Authentication");
        }

        filterChain.doFilter(request, response);
    }

    private Optional<String> getBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        String token = bearerToken != null && bearerToken.startsWith("Bearer ") ?
                bearerToken.substring(7) : null;
        return Optional.ofNullable(token);
    }
}
