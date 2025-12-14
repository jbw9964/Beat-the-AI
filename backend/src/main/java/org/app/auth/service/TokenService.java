package org.app.auth.service;

import io.jsonwebtoken.*;
import java.util.function.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.auth.domain.token.*;
import org.app.auth.dto.response.*;
import org.app.auth.repository.*;
import org.app.config.security.*;
import org.app.entity.*;
import org.app.util.*;
import org.app.util.exception.*;
import org.springframework.stereotype.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService implements UserPrincipalProvider {

    private final GlobalUtil globalUtil;
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
            return globalUtil.getNonSoftDeltedOrThrow(
                    userId, userRepo::findById, this::invalidTokenEx
            );
        } catch (JwtException e) {
            log.warn(e.getMessage(), e);
            throw this.invalidTokenEx();
        } catch (Exception e) {
            log.warn("Unexpected error occurred while trying to get user from token", e);
            throw this.invalidTokenEx();
        }
    }

    public User getUserFromRt(String refreshToken) {
        Long userId;

        try {
            userId = rtManager.getClaimsFrom(refreshToken).getUserId();
            return rtRecordRepo.findById(userId)
                    .filter(rtRecord -> rtRecord.getToken().equals(refreshToken))
                    .flatMap(rtRecord -> userRepo.findById(userId))
                    .filter(Predicate.not(User::withdrawn))
                    .orElseThrow(this::invalidTokenEx);
        } catch (JwtException e) {
            log.warn(e.getMessage(), e);
            throw this.invalidTokenEx();
        } catch (Exception e) {
            log.warn("Unexpected error occurred while trying to get user from token", e);
            throw this.invalidTokenEx();
        }
    }

    private UnauthorizedException invalidTokenEx() {
        return new UnauthorizedException("토큰이 유효하지 않습니다.");
    }

    @Override
    public User findByAccessToken(String accessToken) {
        return this.getUserFromAt(accessToken);
    }
}
