package org.app.auth.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.app.auth.domain.exception.*;
import org.app.auth.dto.response.*;
import org.app.auth.repository.*;
import org.app.entity.*;
import org.app.util.*;
import org.app.util.exception.*;
import org.springframework.dao.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleAuthService {

    private final GlobalUtil globalUtil;
    private final AuthUserRepository userRepo;
    private final TokenService tokenService;
    private final PasswordEncoder pwEncoder;

    public Tokens loginWithIdPw(String loginId, String password) {

        User find = globalUtil.getNonSoftDeltedOrThrow(
                loginId, userRepo::findByLoginId, this::loginFailEx
        );

        if (!pwEncoder.matches(password, find.getEncryptedPassword())) {
            throw this.loginFailEx();
        }

        return tokenService.createTokensWith(find);
    }

    @Transactional
    public Long idPwSignup(String username, String loginId, String password, String email) {

        String encryptedPw = pwEncoder.encode(password);
        User newUser = new User(username, loginId, encryptedPw);
        newUser.changeEmail(email);

        try {
            // 확실히 동시성 관련한 고민거리 대부분을 없애주지만
            // 정말 down-side 는 없을지 궁금하네..
            return userRepo.save(newUser).getId();
        } catch (DataIntegrityViolationException e) {
            log.warn("Failed to save entity: {}", e.getMessage(), e);
            throw new DuplicateLoginIDException();
        }
    }

    public Tokens reissueViaRt(String refreshToken) {
        User user = tokenService.getUserFromRt(refreshToken);

        return tokenService.createTokensWith(user);
    }

    private ForbiddenException loginFailEx() {
        return new ForbiddenException("로그인에 실패하였습니다. ID, PW 를 확인해주세요.");
    }

}
