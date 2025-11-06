package org.app.auth.service;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.*;
import org.app.auth.domain.exception.*;
import org.app.auth.dto.response.*;
import org.app.auth.repository.*;
import org.app.entity.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.security.crypto.password.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class SimpleAuthServiceTest extends IntegrationTestSupport {

    static final String username = "testNAME",
            loginId = "testID",
            password = "testPW",
            email = "testEMAIL";
    static final String existingUsername = "ex",
            existingLoginId = "ex",
            existingPassword = "exPW";
    static Long exisingUserId;
    @Autowired
    SimpleAuthService simpleAuthService;

    @Autowired
    AuthUserRepository authUserRepo;

    @Autowired
    PasswordEncoder pwEncoder;

    @BeforeEach
    void setUp() {
        exisingUserId = simpleAuthService.idPwSignup(
                existingUsername, existingLoginId, existingPassword, null
        );
    }

    @AfterEach
    void tearDown() {
        authUserRepo.deleteAll();
    }

    @Test
    @DisplayName("ID, PW 를 통해 로그인할 수 있다.")
    void loginWithIdPw() {
        Tokens tokens = simpleAuthService.loginWithIdPw(existingLoginId, existingPassword);

        assertThat(tokens).isNotNull().hasNoNullFieldsOrProperties();
        assertThat(tokens.accessToken()).isNotEmpty();
        assertThat(tokens.refreshToken()).isNotEmpty();
    }

    @Test
    @DisplayName("ID, PW 를 통해 회원가입할 수 있다.")
    void idPwSignup() {
        Long userId = simpleAuthService.idPwSignup(username, loginId, password, email);

        assertThat(userId).isNotNull();

        Optional<User> find = authUserRepo.findById(userId);
        assertThat(find).isNotEmpty();

        User user = find.get();
        assertThat(user.getId()).isEqualTo(userId);
        assertThat(user.getName()).isEqualTo(username);
        assertThat(user.getEmail()).isEqualTo(email);

        assertThat(user.getLoginId()).isEqualTo(loginId);
        assertThat(pwEncoder.matches(password, user.getEncryptedPassword())).isTrue();
    }

    @Test
    @DisplayName("RT 를 통해 토큰을 재발급 받을 수 있다.")
    void reissueViaRt() {
        String rt = simpleAuthService.loginWithIdPw(existingLoginId, existingPassword)
                .refreshToken();

        Tokens tokens = simpleAuthService.reissueViaRt(rt);

        assertThat(tokens).isNotNull().hasNoNullFieldsOrProperties();
        assertThat(tokens.accessToken()).isNotEmpty();
        assertThat(tokens.refreshToken()).isNotEmpty();
    }

    @Test
    @DisplayName("ID, PW 가 다르면 로그인에 실패한다.")
    void testInvalidIdPw() {
        String invalid = "invalid";

        assertThatThrownBy(() -> simpleAuthService.loginWithIdPw(invalid, existingPassword))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> simpleAuthService.loginWithIdPw(existingLoginId, invalid))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("중복된 ID 로 회원가입할 수 없다.")
    void testDuplicateIdSignup() {
        assertThatThrownBy(
                () -> simpleAuthService.idPwSignup(username, existingLoginId, password, email))
                .isInstanceOf(DuplicateLoginIDException.class);
    }
}