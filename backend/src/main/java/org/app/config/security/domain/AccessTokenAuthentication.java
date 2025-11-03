package org.app.config.security.domain;

import java.util.*;
import org.springframework.security.core.*;

public record AccessTokenAuthentication(String accessToken) implements Authentication {

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.accessToken();
    }

    @Override
    public boolean isAuthenticated() {
        return false;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException("Cannot set authentication manually");
    }

    @Override
    public String getName() {
        return String.format("ACCESS_TOKEN=%s", accessToken);
    }
}
