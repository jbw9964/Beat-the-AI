package org.app.auth.service;

import lombok.*;
import org.app.auth.domain.exception.*;
import org.app.auth.dto.*;
import org.app.auth.repository.*;
import org.app.entity.*;
import org.app.util.exception.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class SimpleAuthService {

    private final AuthUserRepository userRepo;
    private final TokenService tokenService;
    private final PasswordEncoder pwEncoder;

    public Tokens loginWithIdPw(String loginId, String password) {

        User find = userRepo.findByLoginId(loginId)
                .orElseThrow(this::loginFailEx);

        if (!pwEncoder.matches(password, find.getEncryptedPw())) {
            throw this.loginFailEx();
        }

        return tokenService.createTokensWith(find);
    }

    @Transactional
    public Long idPwSignup(String username, String loginId, String password, String email) {

        if (userRepo.findByLoginId(loginId).isPresent()) {
            throw new DuplicateLoginIDException();
        }

        String encryptedPw = pwEncoder.encode(password);
        User newUser = new User(username, loginId, encryptedPw);
        newUser.changeEmail(email);
        userRepo.save(newUser);

        return newUser.getId();
    }

    public Tokens reissueViaRt(String refreshToken) {
        User user = tokenService.getUserFromRt(refreshToken);

        return tokenService.createTokensWith(user);
    }

    private ForbiddenException loginFailEx() {
        return new ForbiddenException("로그인에 실패하였습니다. ID, PW 를 확인해주세요.");
    }

}
