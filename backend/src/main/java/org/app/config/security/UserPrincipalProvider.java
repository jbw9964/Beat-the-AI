package org.app.config.security;

import org.app.entity.*;
import org.springframework.modulith.*;

@NamedInterface
public interface UserPrincipalProvider {

    User findByAccessToken(String accessToken);
}
