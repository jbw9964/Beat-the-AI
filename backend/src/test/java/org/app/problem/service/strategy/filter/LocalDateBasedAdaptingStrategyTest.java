package org.app.problem.service.strategy.filter;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.time.format.*;
import java.util.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

@SuppressWarnings("UnnecessaryLocalVariable")
class LocalDateBasedAdaptingStrategyTest {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            AbstractFilterRequestAdaptingStrategy.DATE_TIME_FORMATTER;

    private static final LocalDate NOW = LocalDate.now();

    private static FilteringRequest genReq(
            ProblemFilterType filterType,
            LocalDate from, LocalDate to, LocalDate equalTo
    ) {
        return Utils.genRequest(
                filterType, from, to, equalTo,
                DATE_TIME_FORMATTER::format
        );
    }

    private static LocalDate getMonthBeforeFromNowOrNull(Number minus) {
        return minus == null ? null : NOW.minusMonths(minus.longValue());
    }

    private static LocalDate getMonthAfterFromNowOrNull(Number minus) {
        return minus == null ? null : NOW.plusMonths(minus.longValue());
    }

    private static LocalDateBasedAdaptingStrategy genStrategy(
            ProblemFilterType filterType, LocalDate minT, LocalDate maxT,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        return new LocalDateBasedAdaptingStrategy(
                minT, maxT, useFrom, useTo, useEqualTo
        ) {
            @Override
            public ProblemFilterType handleableFilterType() {
                return filterType;
            }
        };
    }

    @Test
    @DisplayName("LocalDate 기반 전략의 comparaotr 가 올바르다.")
    void testComparator() {
        LocalDateBasedAdaptingStrategy strategy = genStrategy(
                null, null, null, true, true, true
        );

        LocalDate lower = NOW.minusMonths(1);
        LocalDate median = NOW;
        LocalDate higher = NOW.plusMonths(1);

        Utils.assertComparator(strategy, lower, median, higher);
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#integerBaseToFilterArguments")
    @DisplayName("LocaDate 기반 전략의 toFilter 가 정상 작동한다.")
    void testToFilter(
            ProblemFilterType filterType, Number from, Number to, Number equalTo,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        for (int i = 0; i < 2; i++) {

            boolean testWithThreshold = (i & 0b1) == 0b1;

            LocalDate fromLD = getMonthBeforeFromNowOrNull(from);
            LocalDate toLD = getMonthAfterFromNowOrNull(to);
            LocalDate equalToLD = getMonthAfterFromNowOrNull(equalTo);

            LocalDate expectedFrom = !useFrom ? null : fromLD;
            LocalDate expectedTo = !useTo ? null : toLD;
            LocalDate expectedEqualTo = !useEqualTo ? null : equalToLD;

            LocalDate minT, maxT;

            if (testWithThreshold) {
                Comparator<LocalDate> comparator = LocalDate::compareTo;
                minT = Utils.getMinima(comparator, fromLD, toLD, equalToLD);
                maxT = Utils.getMaxima(comparator, fromLD, toLD, equalToLD);
            } else {
                minT = maxT = null;
            }

            LocalDateBasedAdaptingStrategy strategy = genStrategy(
                    filterType, minT, maxT, useFrom, useTo, useEqualTo
            );

            FilteringRequest request = genReq(filterType, fromLD, toLD, equalToLD);

            Utils.assertToFilterResponseEquality(
                    request, strategy,
                    expectedFrom, expectedTo, expectedEqualTo
            );
        }

    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#problemFilterTypes")
    @DisplayName("전략과 요청의 filter type 이 일치하지 않으면 FilterTypeMismatchException 이 발생한다.")
    void testFilterTypeMismatch(ProblemFilterType given) {
        ProblemFilterType anyOtherFilterType = Utils.getAnyOtherFilterType(given);
        LocalDateBasedAdaptingStrategy strategy = genStrategy(
                anyOtherFilterType, null, null, true, true, true
        );

        FilteringRequest request = genReq(
                given, null, null, null
        );

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(FilterTypeMismatchException.class);
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#problemFilterTypes")
    @DisplayName("LocaDate 로 parsing 할 수 없는 요청은 MalformedFilteringRequestException 을 일으킨다.")
    void testMalformedRequest(ProblemFilterType filterType) {
        LocalDateBasedAdaptingStrategy strategy = genStrategy(
                filterType, null, null, true, true, true
        );

        String invalid = "This is not a number";
        FilteringRequest request = new FilteringRequest(
                filterType, invalid, invalid, invalid
        );

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(MalformedFilteringRequestException.class);
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#problemFilterTypes")
    @DisplayName("허용 범위 밖 값들이 제공되면 IllegalFilterValueException 이 발생한다.")
    void testIllegalFilterValue1(ProblemFilterType filterType) {

        LocalDate minT = getMonthAfterFromNowOrNull(10L);
        LocalDate maxT = getMonthAfterFromNowOrNull(20L);
        LocalDateBasedAdaptingStrategy strategy = genStrategy(
                filterType, minT, maxT, true, true, true
        );

        LocalDate validValue = getMonthAfterFromNowOrNull(15L);
        long[] invalidValues = {
                1L, 5L, 9L,
                21L, 25L, 30L
        };
        for (long invalid : invalidValues) {

            for (int i = 0; i < 2; i++) {

                boolean testBefore = (i & 0b1) == 0b1;

                var invalidValue = testBefore ?
                        getMonthBeforeFromNowOrNull(invalid) :
                        getMonthAfterFromNowOrNull(invalid);

                var req1 = genReq(filterType, invalidValue, validValue, validValue);
                var req2 = genReq(filterType, validValue, invalidValue, validValue);
                var req3 = genReq(filterType, validValue, validValue, invalidValue);
                var req4 = genReq(filterType, invalidValue, invalidValue, validValue);
                var req5 = genReq(filterType, validValue, invalidValue, invalidValue);
                var req6 = genReq(filterType, invalidValue, invalidValue, invalidValue);

                var reqs = Arrays.asList(req1, req2, req3, req4, req5, req6);
                for (var req : reqs) {
                    assertThatThrownBy(() -> strategy.toFilter(req))
                            .isInstanceOf(IllegalFilterValueException.class);
                }
            }
        }
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#problemFilterTypes")
    @DisplayName("From, To 가 활성화되고 from 이 to 보다 크면 IllegalFilterValueException 가 발생한다.")
    void testIllegalFilterValue2(ProblemFilterType filterType) {
        LocalDateBasedAdaptingStrategy strategy = genStrategy(
                filterType, null, null, true, true, true
        );

        LocalDate from = getMonthAfterFromNowOrNull(1L);
        LocalDate to = getMonthBeforeFromNowOrNull(1L);
        FilteringRequest request = genReq(filterType, from, to, null);

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(IllegalFilterValueException.class);
    }

    @Test
    @DisplayName("허용 값 범위가 올바르지 않으면 class 생성 시 IllegalStateException 이 발생한다.")
    void testIllegalStateException() {
        LocalDate minT = getMonthAfterFromNowOrNull(10L);
        LocalDate maxT = getMonthBeforeFromNowOrNull(10L);

        assertThatThrownBy(() -> genStrategy(
                null, minT, maxT,
                true, true, true
        ))
                .isInstanceOf(IllegalStateException.class);
    }
}