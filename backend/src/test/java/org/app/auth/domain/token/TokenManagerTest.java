package org.app.auth.domain.token;

import static org.assertj.core.api.Assertions.*;

import lombok.extern.slf4j.*;
import org.*;
import org.app.auth.repository.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class TokenManagerTest extends IntegrationTestSupport {

    static User testUser;

    @Autowired
    AccessTokenManager atManager;

    @Autowired
    RefreshTokenManager rtManager;

    @Autowired
    AuthUserRepository authUserRepo;

    @BeforeEach
    void setUp() {
        testUser = new User("test");
        authUserRepo.save(testUser);
    }

    @AfterEach
    void tearDown() {
        authUserRepo.deleteAll();
    }

    @Test
    @DisplayName("토큰 매니저로 토큰을 생성할 수 있다.")
    void createTokenWith() {
        String at = atManager.createTokenWith(testUser);
        String rt = rtManager.createTokenWith(testUser);

        assertThat(at).isNotEmpty();
        assertThat(rt).isNotEmpty();

        log.info("at: {}", at);
        log.info("rt: {}", rt);
    }

    @Test
    @DisplayName("토큰으로부터 payload 를 가져올 수 있다.")
    void getClaimsFrom() {
        String at = atManager.createTokenWith(testUser);
        String rt = rtManager.createTokenWith(testUser);

        CustomJwtPayloadClaims atClaims = atManager.getClaimsFrom(at);
        CustomJwtPayloadClaims rtClaims = rtManager.getClaimsFrom(rt);

        CustomJwtPayloadClaims atExpected = new CustomJwtPayloadClaims(testUser);
        CustomJwtPayloadClaims rtExpected = new CustomJwtPayloadClaims(testUser);

        assertThat(atClaims).isNotNull().isEqualTo(atExpected);
        assertThat(rtClaims).isNotNull().isEqualTo(rtExpected);
    }
}