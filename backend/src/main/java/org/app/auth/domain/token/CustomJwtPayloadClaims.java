package org.app.auth.domain.token;

import io.jsonwebtoken.*;
import lombok.*;
import org.app.auth.domain.exception.*;
import org.app.entity.*;

@Getter
@EqualsAndHashCode
public final class CustomJwtPayloadClaims {

    private final String sub;

    public CustomJwtPayloadClaims(@NonNull User user) {
        this.sub = String.valueOf(user.getId());
    }

    public CustomJwtPayloadClaims(Claims payloadClaims) {
        String sub = payloadClaims.getSubject();

        if (sub == null || sub.isEmpty()) {
            throw new InvalidCustomJwtClaimException(
                    "No subject exists in jwt payload claims"
            );
        }

        try {
            Long.parseLong(sub);
        } catch (NumberFormatException e) {
            throw new InvalidCustomJwtClaimException(
                    "Subject exists in jwt, but somehow not compatible"
            );
        }

        this.sub = sub;
    }

    public ClaimsBuilder buildCustomClaims() {
        return Jwts.claims()
                .subject(this.sub);
    }

    public Long getUserId() {
        return Long.parseLong(this.sub);
    }

}
