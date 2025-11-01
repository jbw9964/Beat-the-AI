package org.app.config.security;

import jakarta.servlet.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
@SuppressWarnings("UnusedReturnValue")
class Util {

    public Util disableFormLogin(HttpSecurity http) throws Exception {
        http.formLogin(FormLoginConfigurer::disable);
        return this;
    }

    public Util disableHttpBasic(HttpSecurity http) throws Exception {
        http.httpBasic(HttpBasicConfigurer::disable);
        return this;
    }

    public Util disableLogout(HttpSecurity http) throws Exception {
        http.logout(LogoutConfigurer::disable);
        return this;
    }

    public Util disableCsrf(HttpSecurity http) throws Exception {
        http.csrf(CsrfConfigurer::disable);
        return this;
    }

    public Util disableRequestCache(HttpSecurity http) throws Exception {
        http.requestCache(RequestCacheConfigurer::disable);
        return this;
    }

    public Util disableSessionManagement(HttpSecurity http) throws Exception {
        http.sessionManagement(SessionManagementConfigurer::disable);
        return this;
    }

    public Util disableAnonymous(HttpSecurity http) throws Exception {
        http.anonymous(AnonymousConfigurer::disable);
        return this;
    }

    public Util disableSecurityContext(HttpSecurity http) throws Exception {
        http.securityContext(SecurityContextConfigurer::disable);
        return this;
    }

    public Util disableHeaders(HttpSecurity http) throws Exception {
        http.headers(HeadersConfigurer::disable);
        return this;
    }

    public SecurityFilterChain buildAndLogFilters(HttpSecurity http) throws Exception {

        DefaultSecurityFilterChain build = http.build();

        log.info("Security filter chain : {}", build);

        List<Filter> filters = build.getFilters();

        for (int i = 0; i < filters.size(); i++) {
            Filter filter = filters.get(i);
            String simpleName = filter.getClass().getSimpleName();
            log.info("[{}] : {} - {}", i, simpleName, filter.toString());
        }

        return build;
    }
}
