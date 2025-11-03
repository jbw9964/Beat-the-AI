package org.app.config.security.domain;

import java.util.*;
import lombok.*;
import org.springframework.security.core.*;
import org.springframework.security.core.authority.*;

public record SimpleUserAuthentication(Long userId) implements Authentication {

    public SimpleUserAuthentication(@NonNull Long userId) {
        this.userId = userId;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(Roles.USER.getAuthority()));
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
    public Long getPrincipal() {
        return this.userId();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new IllegalArgumentException("Cannot set authentication manually");
    }

    @Override
    public String getName() {
        return String.format("USER_ID=%d", this.userId);
    }

}
