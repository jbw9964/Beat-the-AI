package org.app.config.security.domain;

import lombok.*;
import org.springframework.modulith.*;

@Getter
@NamedInterface
@RequiredArgsConstructor
public enum Roles {
    USER("ROLE_USER"), ANONYMOUS("ROLE_ANONYMOUS");

    final String authority;

}
