package org.app;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.stream.*;
import org.*;
import org.app.util.api.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.context.annotation.*;
import org.springframework.test.web.servlet.*;

@AutoConfigureMockMvc
@Import(PagingTestController.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class PagingTest extends IntegrationTestSupport {

    static final String BASE_URL = PagingTestController.PAGING_TEST_URL;

    @Autowired
    MockMvc mvc;

    private static Stream<Arguments> missingPageRequestParam() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(5, null),
                Arguments.of(null, 2)
        );
    }

    private static Stream<Arguments> validPageRequestParam() {
        return Stream.of(
                Arguments.of(0, 1),
                Arguments.of(1, 1),
                Arguments.of(2, 30)
        );
    }

    private static Stream<Arguments> invalidPageRequestParam() {
        return Stream.of(
                Arguments.of(-1, null),
                Arguments.of(-1, 5),
                Arguments.of(-1, -1),
                Arguments.of(-1, 0),
                Arguments.of(null, -1),
                Arguments.of(0, -1),
                Arguments.of(null, 0),
                Arguments.of(0, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("missingPageRequestParam")
    @DisplayName("파라미터를 제공하지 않아도 기본값이 설정된다.")
    void testDefaultPaging(Integer pageNum, Integer pageSize) throws Exception {
        String url = buildUrl(pageNum, pageSize);
        SimplePageRequest expected = new SimplePageRequest(pageNum, pageSize);

        assertOkAndEquality(url, expected).andDo(print());
    }

    @ParameterizedTest
    @MethodSource("validPageRequestParam")
    @DisplayName("파라미터를 제공하면 정상적으로 인식된다.")
    void testPaging(Integer pageNum, Integer pageSize) throws Exception {
        String url = buildUrl(pageNum, pageSize);
        SimplePageRequest expected = new SimplePageRequest(pageNum, pageSize);

        assertThat(expected.getPageNumOrDefault()).isEqualTo(pageNum);
        assertThat(expected.getPageSizeOrDefault()).isEqualTo(pageSize);

        assertOkAndEquality(url, expected).andDo(print());
    }

    @ParameterizedTest
    @MethodSource("invalidPageRequestParam")
    @DisplayName("올바른 파라미터를 제공하지 않으면 400 응답을 받는다.")
    void testInvalidPaging(Integer pageNum, Integer pageSize) throws Exception {
        String url = buildUrl(pageNum, pageSize);

        assertBadRequest(url).andDo(print());
    }

    ResultActions assertOkAndEquality(
            String url, SimplePageRequest pageRequest
    ) throws Exception {
        int pageNum = pageRequest.getPageNumOrDefault();
        int pageSize = pageRequest.getPageSizeOrDefault();

        return mvc.perform(get(url, pageRequest))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.data.pageNum").value(pageNum)
                )
                .andExpect(
                        jsonPath("$.data.pageSize").value(pageSize)
                );
    }

    ResultActions assertBadRequest(String url) throws Exception {
        return mvc.perform(get(url))
                .andExpect(status().isBadRequest());
    }

    String buildUrl(Integer pageNum, Integer pageSize) {
        StringBuilder sb = new StringBuilder(BASE_URL);
        String pn = pageNum != null ? String.format("pageNum=%s", pageNum) : "";
        String ps = pageSize != null ? String.format("pageSize=%s", pageSize) : "";

        if (pageNum == null && pageSize == null) {
            return sb.toString();
        }

        sb.append('?');

        if (pageNum == null) {
            return sb.append(ps).toString();
        }

        if (pageSize == null) {
            return sb.append(pn).toString();
        }

        return sb.append(pn).append('&').append(ps).toString();
    }
}
