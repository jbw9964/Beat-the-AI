package org.app;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.*;
import org.app.auth.repository.*;
import org.app.auth.service.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.test.web.servlet.*;
import org.springframework.test.web.servlet.request.*;

@AutoConfigureMockMvc
@Import(AuthTestController.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class AuthTest extends IntegrationTestSupport {

    static final String BASE_URL = "/api/auth-testing";
    static User testUser1, testUser2;

    @Autowired
    MockMvc mvc;

    @Autowired
    AuthUserRepository authUserRepo;

    @Autowired
    TokenService tokenService;

    @BeforeEach
    void setUp() {
        testUser1 = new User("test1");
        authUserRepo.save(testUser1);
        testUser2 = new User("test2");
        authUserRepo.save(testUser2);
    }

    @AfterEach
    void tearDown() {
        authUserRepo.deleteAll();
    }

    @Test
    @DisplayName("인증이 필요없는 API 를 사용할 수 있다.")
    void onPublic() throws Exception {
        String url = String.format("%s/public", BASE_URL);

        assertOk(
                doRequest(url, null)
        );
    }

    @Test
    @DisplayName("ANONYMOUS 인가가 필요한 API 를 사용할 수 있다.")
    void onAnonymous() throws Exception {
        String url = String.format("%s/anonymous", BASE_URL);

        assertOk(
                doRequest(url, null)
        );
    }

    @Test
    @DisplayName("USER 인가가 필요한 API 를 사용할 수 있다.")
    void onUser() throws Exception {
        Long userId = testUser1.getId();

        String url = String.format("%s/user", BASE_URL);
        String token = tokenService.createTokensWith(testUser1).accessToken();

        assertOk(
                doRequest(url, token)
        )
                .andExpect(
                        jsonPath("$.data").value(String.valueOf(userId))
                );
    }

    @Test
    @DisplayName("자기 자신만 접근할수 있는 API 를 사용할 수 있다.")
    void onPrivate() throws Exception {
        Long userId = testUser1.getId();
        String url = String.format("%s/user/%d", BASE_URL, userId);
        String token = tokenService.createTokensWith(testUser1).accessToken();

        assertOk(
                doRequest(url, token)
        )
                .andExpect(
                        jsonPath("$.data").value(String.valueOf(userId))
                );
    }

    @Test
    @DisplayName("인증이 부족하면 401 코드를 받는다.")
    void testUnauthorized() throws Exception {
        int code = HttpStatus.UNAUTHORIZED.value();
        String url1 = String.format("%s/user", BASE_URL);
        String url2 = String.format("%s/user/1", BASE_URL);

        assertStatus(
                doRequest(url1, null), code
        );
        assertStatus(
                doRequest(url2, null), code
        );
    }

    @Test
    @DisplayName("인가가 부족하면 403 코드를 받는다.")
    void testAccessDenied() throws Exception {
        int code = HttpStatus.FORBIDDEN.value();

        Long anotherUserId = testUser2.getId();
        String url = String.format("%s/user/%d", BASE_URL, anotherUserId);
        String token = tokenService.createTokensWith(testUser1).accessToken();

        assertStatus(
                doRequest(url, token), code
        );
    }

    ResultActions doRequest(String url, String token) throws Exception {
        MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get(url);

        if (token != null) {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        }

        return mvc.perform(requestBuilder);
    }

    ResultActions assertStatus(ResultActions mvc, int status) throws Exception {
        return mvc.andExpect(status().is(status));
    }

    ResultActions assertOk(ResultActions mvc) throws Exception {
        return assertStatus(mvc, 200);
    }
}