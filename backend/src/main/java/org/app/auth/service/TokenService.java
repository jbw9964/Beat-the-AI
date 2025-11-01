package org.app.auth.service;

import lombok.*;
import lombok.extern.slf4j.*;
import org.app.auth.domain.token.*;
import org.app.auth.dto.*;
import org.app.auth.repository.*;
import org.app.entity.*;
import org.app.util.exception.*;
import org.springframework.modulith.*;
import org.springframework.stereotype.*;

@Slf4j
@Service
@NamedInterface
@RequiredArgsConstructor
public class TokenService {

    private final AccessTokenManager atManager;
    private final RefreshTokenManager rtManager;
    private final AuthUserRepository userRepo;
    private final RTRecordRepository rtRecordRepo;

    public Tokens createTokensWith(User user) {
        String at = atManager.createTokenWith(user);
        String rt = rtManager.createTokenWith(user);

        Long userId = user.getId();
        rtRecordRepo.deleteById(userId);
        RTRecord newRecord = new RTRecord(userId, rt, rtManager.getExpiration());
        rtRecordRepo.save(newRecord);

        return new Tokens(at, rt);
    }

    public User getUserFromAt(String accessToken) {
        Long userId;

        try {
            userId = atManager.getClaimsFrom(accessToken).getUserId();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw this.invalidTokenEx();
        }

        return userRepo.findById(userId).orElseThrow(this::invalidTokenEx);
    }

    public User getUserFromRt(String refreshToken) {
        Long userId;

        try {
            userId = rtManager.getClaimsFrom(refreshToken).getUserId();
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            throw this.invalidTokenEx();
        }

        return rtRecordRepo.findById(userId)
                .filter(rtRecord -> rtRecord.getToken().equals(refreshToken))
                .flatMap(rtRecord -> userRepo.findById(userId))
                .orElseThrow(this::invalidTokenEx);
    }

    private UnauthorizedException invalidTokenEx() {
        return new UnauthorizedException("토큰이 유효하지 않습니다.");
    }
}
