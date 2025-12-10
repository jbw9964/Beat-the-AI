package org.app.problem.domain.search.filter;

import static org.assertj.core.api.Assertions.*;

import java.util.stream.*;
import org.app.problem.dto.request.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class ProblemFilterTypeTest {

    private static final String STR = "hello";

    private static Stream<Arguments> getValidArgs() {
        return Stream.of(
                Arguments.of(
                        ProblemFilterType.CREATED_TIME, STR, null, null
                ),
                Arguments.of(
                        ProblemFilterType.CREATED_TIME, null, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.CREATED_TIME, STR, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.CREATED_USER, null, null, STR
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_REWARDS, STR, null, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_REWARDS, null, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_REWARDS, STR, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS, STR, null, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS, null, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS, STR, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PROBLEM_SCENARIOS, STR, null, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PROBLEM_SCENARIOS, null, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PROBLEM_SCENARIOS, STR, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_RATINGS, STR, null, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_RATINGS, null, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_RATINGS, STR, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.RATING_AVG, STR, null, null
                ),
                Arguments.of(
                        ProblemFilterType.RATING_AVG, null, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.RATING_AVG, STR, STR, null
                )
        );
    }

    private static Stream<Arguments> getInvalidArgs() {
        return Stream.of(
                Arguments.of(
                        ProblemFilterType.CREATED_TIME, null, null, STR
                ),
                Arguments.of(
                        ProblemFilterType.CREATED_USER, STR, STR, null
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_REWARDS, null, null, STR
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS, null, null, STR
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PROBLEM_SCENARIOS, null, null, STR
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_RATINGS, null, null, STR
                ),
                Arguments.of(
                        ProblemFilterType.RATING_AVG, null, null, STR
                )
        );
    }

    @ParameterizedTest
    @MethodSource("getValidArgs")
    @DisplayName("필터링 종류에 따른 변수가 유효할 때 에러가 발생하지 않는다.")
    void testValidCase(ProblemFilterType filterType, String from, String to, String equalTo) {
        FilteringRequest request = new FilteringRequest(filterType, from, to, equalTo);

        assertThatCode(() -> ProblemFilterType.assertValidParams(request))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @MethodSource("getInvalidArgs")
    @DisplayName("필터링 종류에 따른 변수가 유효하지 않으면 BadRequestException 에러가 발생하지 않는다.")
    void testInvalidCase(ProblemFilterType filterType, String from, String to, String equalTo) {
        FilteringRequest request = new FilteringRequest(filterType, from, to, equalTo);

        assertThatThrownBy(() -> ProblemFilterType.assertValidParams(request))
                .isInstanceOf(BadRequestException.class);
    }
}