package org.app.auth.controller;

import jakarta.validation.*;
import lombok.*;
import org.app.auth.dto.*;
import org.app.auth.dto.request.*;
import org.app.auth.dto.response.*;
import org.app.auth.service.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
class AuthController {

    private final SimpleAuthService simpleAuthService;

    @PostMapping("/login")          // id pw 로그인
    public ApiResponse<Tokens> login(
            @Valid @RequestBody AuthLoginRequest req
    ) {
        String loginId = req.loginId();
        String password = req.password();

        Tokens tokens = simpleAuthService.loginWithIdPw(loginId, password);
        return ApiResponse.success(tokens);
    }

    @PostMapping("/signup")         // 쌩 회원가입
    public ApiResponse<AuthSignupResponse> signup(
            @Valid @RequestBody AuthSignupRequest req
    ) {
        String username = req.name();
        String loginId = req.loginId();
        String password = req.password();
        String email = req.email();

        Long userId = simpleAuthService.idPwSignup(username, loginId, password, email);

        return ApiResponse.created(new AuthSignupResponse(userId));
    }

    @PostMapping("/reissue")        // 토큰 재발급
    public ApiResponse<Tokens> reissue(
            @Valid @RequestBody AuthReissueRequest req
    ) {
        String rt = req.refreshToken();

        Tokens tokens = simpleAuthService.reissueViaRt(rt);
        return ApiResponse.success(tokens);
    }

    @PostMapping("/oidc/login")     // oidc 로그인
    public ApiResponse<?> oidcLogin() {
        // TODO : OIDC 로그인 구현
        throw new NotImplementedException("OIDC 로그인 미구현");
    }

    @PostMapping("/oidc/signup")    // oidc 회원가입
    public ApiResponse<?> oidcSignup() {
        // TODO : OIDC 회원가입 구현
        throw new NotImplementedException("OIDC 회원가입 미구현");
    }

    @PostMapping("/oidc/connect")   // oidc 연동
    public ApiResponse<?> oidcConnect() {
        // TODO : OIDC 연동 구현
        throw new NotImplementedException("OIDC 연동 미구현");
    }
}
