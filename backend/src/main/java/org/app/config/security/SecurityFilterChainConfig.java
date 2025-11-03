package org.app.config.security;

import lombok.*;
import lombok.extern.slf4j.*;
import org.app.config.security.dto.*;
import org.app.config.security.filter.*;
import org.springframework.boot.web.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.core.*;
import org.springframework.core.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.builders.*;
import org.springframework.security.config.annotation.method.configuration.*;
import org.springframework.security.config.annotation.web.builders.*;
import org.springframework.security.config.annotation.web.configuration.*;
import org.springframework.security.config.annotation.web.configurers.*;
import org.springframework.security.web.*;
import org.springframework.security.web.authentication.*;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityFilterChainConfig {

    private static final String USER_AUTHORITY = Roles.USER.getAuthority();

    private static final String ANONYMOUS_PRINCIPAL = "I'm anonymous user!";
    private static final String ANONYMOUS_AUTHORITY = Roles.ANONYMOUS.getAuthority();

    private final Util util;
    private final BypassingJwtAuthProvider bypassingJwtAuthProvider;

    private final ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;
    private final ApiAccessDeniedHandler apiAccessDeniedHandler;

    @Bean
    @Order(Ordered.LOWEST_PRECEDENCE)
    @SuppressWarnings("DefaultAnnotationParam")
    public SecurityFilterChain defaultSecurityChainConfig(HttpSecurity http) throws Exception {

        util
                .disableHttpBasic(http)
                .disableLogout(http)
                .disableFormLogin(http)
                .disableCsrf(http)
                .disableAnonymous(http)
                .disableSecurityContext(http)
                .disableHeaders(http)
        ;

        // ?? 이거 없으면 test 시 context load 실패함... 왜?????
        // 없어도 app run 은 잘되는데?? 왜어째서나니더퍽??
        http.authenticationManager(authenticationManagerBean(http));

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api-doc").permitAll()
                        .requestMatchers("/v3/api-docs").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .anyRequest().denyAll()
                );

        return util.buildAndLogFilters(http);
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public SecurityFilterChain apiSecurityFilterChainConfig(HttpSecurity http) throws Exception {

        util
                .disableSessionManagement(http)
                .disableHttpBasic(http)
                .disableLogout(http)
                .disableFormLogin(http)
                .disableCsrf(http)
                .disableRequestCache(http)
        ;

        http.authenticationManager(authenticationManagerBean(http));

        // cors config : 나중에 설정
        http.cors(CorsConfigurer::disable);

        // 익명 사용자 config : AnonymousAuthenticationFilter 참고
        // 몰랐는데 Anonymous 로 인증받아도 auth 필요한 endpoint 는
        // AuthorizationManager 가 ex 발생, ExceptionTranslationFilter 가 AuthEntryPoint 로 가게 만듬
        // Anonymous 인 security context 에서 AccessDeniedException 터져도 filter 가 그렇게 가도록 함.
        http.anonymous(ano -> ano
                .authorities(ANONYMOUS_AUTHORITY)
                .principal(ANONYMOUS_PRINCIPAL)
        );

        // endpoint 인증, 인가 config
        http
                .securityMatcher("/api/**")
                .authorizeHttpRequests(auth -> auth

                        // auth domain
                        .requestMatchers(
                                "/api/auth/login", "/api/auth/signup", "/api/auth/reissue"
                        ).permitAll()
                        .requestMatchers(
                                "/oidc/login", "/oidc/signup"
                        ).permitAll()

                        // user doamin
                        .requestMatchers(
                                "/api/user/{user-id:\\d+}",
                                "/api/user/{user-id:\\d+}/public-record",
                                "/api/user/{user-id:\\d+}/public-record/{record-id:\\d+}"
                        ).permitAll()

                        // endpoints for auth testing
                        .requestMatchers(
                                "/api/auth-testing/public"
                        ).permitAll()
                        .requestMatchers(
                                "/api/auth-testing/anonymous"
                        ).hasAuthority(ANONYMOUS_AUTHORITY)
                        .requestMatchers(
                                "/api/auth-testing/user"
                        ).hasAuthority(USER_AUTHORITY)

                        .anyRequest().authenticated()
                )
        ;

        // jwt auth config
        http
                .addFilterBefore(
                        bypassingJwtAuthFilter(authenticationManagerBean(http)),
                        AnonymousAuthenticationFilter.class
                )
                .authenticationProvider(bypassingJwtAuthProvider)
                .exceptionHandling(ehc -> ehc
                        .authenticationEntryPoint(apiAuthenticationEntryPoint)
                        .accessDeniedHandler(apiAccessDeniedHandler)
                )
        ;

        return util.buildAndLogFilters(http);
    }

    @Bean
    public AuthenticationManager authenticationManagerBean(HttpSecurity http) throws Exception {
        AuthenticationManager find = http.getSharedObject(AuthenticationManager.class);
        return find != null ? find :
                http.getSharedObject(AuthenticationManagerBuilder.class).build();
    }

    // Bean 으로 정의하면 Spring MVC 가 자동 구성하는 Servlet filter 에도 속하게 됨.
    // 아마 boot-web 결합한 뭔가때문일 듯. 그래서 아래 bypassingJwtAuthFilterRegistration 로 해결함.
    // 참고 : https://docs.spring.io/spring-boot/how-to/webserver.html#howto.webserver.add-servlet-filter-listener.spring-bean
    @Bean
    public BypassingJwtAuthFilter bypassingJwtAuthFilter(
            AuthenticationManager authenticationManager
    ) {
        return new BypassingJwtAuthFilter(authenticationManager);
    }

    @Bean
    public FilterRegistrationBean<BypassingJwtAuthFilter> bypassingJwtAuthFilterRegistration(
            BypassingJwtAuthFilter filter
    ) {
        FilterRegistrationBean<BypassingJwtAuthFilter> registration
                = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }
}
