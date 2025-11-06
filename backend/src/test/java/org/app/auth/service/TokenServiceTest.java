package org.app.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.auth.dto.response.*;
import org.app.auth.repository.*;
import org.app.entity.*;
import org.app.util.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.test.context.bean.override.mockito.*;

@Slf4j
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class TokenServiceTest extends IntegrationTestSupport {

    static User testUser;
    static User removedUser;

    @Autowired
    AuthUserRepository authUserRepo;

    @Autowired
    RTRecordRepository rtRecordRepo;

    @Autowired
    TokenService tokenService;

    @MockitoSpyBean
    DateTimeProvider dateTimeProvider;

    @BeforeEach
    void setUp() {
        testUser = new User("test");
        authUserRepo.save(testUser);
        removedUser = new User("removed");
        authUserRepo.save(removedUser);
        authUserRepo.deleteById(removedUser.getId());
    }

    @AfterEach
    void tearDown() {
        rtRecordRepo.deleteAll();
        authUserRepo.deleteAll();
    }


    @Test
    @DisplayName("AT, RT 를 생성할 수 있다.")
    void testCreatToken() {
        Tokens resp = tokenService.createTokensWith(testUser);

        assertThat(resp).isNotNull();
        assertThat(resp.accessToken()).isNotEmpty();
        assertThat(resp.refreshToken()).isNotEmpty();
    }

    @Test
    @DisplayName("토큰 생성시 RT 는 저장된다.")
    void testRtRegistry() {
        tokenService.createTokensWith(testUser);

        Optional<RTRecord> find = rtRecordRepo.findById(testUser.getId());
        assertThat(find).isNotEmpty();

        RTRecord record = find.get();
        assertThat(record).isNotNull();
        assertThat(record.getUserId()).isEqualTo(testUser.getId());
        assertThat(record.getToken()).isNotEmpty();
    }

    @Test
    @DisplayName("토큰으로부터 사용자를 식별할 수 있다.")
    void testGetUserFromToken() {
        Tokens tokens = tokenService.createTokensWith(testUser);

        String at = tokens.accessToken();
        String rt = tokens.refreshToken();

        User atFind = tokenService.getUserFromAt(at);
        User rtFind = tokenService.getUserFromRt(rt);

        Long idExpected = testUser.getId();

        assertThat(atFind).isNotNull();
        assertThat(atFind.getId()).isEqualTo(idExpected);
        assertThat(rtFind).isNotNull();
        assertThat(rtFind.getId()).isEqualTo(idExpected);
    }

    @Test
    @DisplayName("토큰이 유효하지 않으면 에러가 발생한다.")
    void testException1() {
        String invalid = "wtf";

        assertThatThrownBy(() -> tokenService.getUserFromAt(invalid))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> tokenService.getUserFromRt(invalid))
                .isInstanceOf(UnauthorizedException.class);

        when(dateTimeProvider.dateNow()).thenReturn(tenYearAgo());

        Tokens tokens = tokenService.createTokensWith(testUser);
        String expiredAt = tokens.accessToken();
        String expiredRt = tokens.refreshToken();

        assertThatThrownBy(() -> tokenService.getUserFromAt(expiredAt))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> tokenService.getUserFromRt(expiredRt))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("토큰이 유효해도 사용자가 존재하지 않으면 에러가 발생한다.")
    void testException2() {
        Tokens tokens = tokenService.createTokensWith(removedUser);
        String at = tokens.accessToken();
        String rt = tokens.refreshToken();

        assertThatThrownBy(() -> tokenService.getUserFromAt(at))
                .isInstanceOf(UnauthorizedException.class);
        assertThatThrownBy(() -> tokenService.getUserFromRt(rt))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("토큰이 유효하고 사용자가 존재해도 RT 가 일치하지 않으면 에러가 발생한다.")
    void testException3() {

        when(dateTimeProvider.dateNow()).thenReturn(justAWhileAgo());
        String prevIssuedRt = tokenService.createTokensWith(testUser).refreshToken();

        when(dateTimeProvider.dateNow()).thenReturn(Date.from(Instant.now()));
        String currentRt = tokenService.createTokensWith(testUser).refreshToken();

        log.info("prevIssuedRt = {}", prevIssuedRt);
        log.info("currentRt = {}", currentRt);

        assertThat(prevIssuedRt).isNotEmpty().isNotEqualTo(currentRt);
        assertThatThrownBy(() -> tokenService.getUserFromRt(prevIssuedRt))
                .isInstanceOf(UnauthorizedException.class);
    }

    private Date tenYearAgo() {
        LocalDate now = LocalDate.now();
        Instant tenYearBefore = now.minusYears(10)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
        return Date.from(tenYearBefore);
    }

    private Date justAWhileAgo() {
        Instant now = Instant.now();
        Instant oneMinBefore = now.minusSeconds(60);
        return Date.from(oneMinBefore);
    }
}