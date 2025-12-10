package org.app.problem.service.strategy.filter;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class LongBasedAdaptingStrategyTest {

    private static FilteringRequest genReq(
            ProblemFilterType filterType, Long from, Long to, Long equalTo
    ) {
        return Utils.genRequest(filterType, from, to, equalTo, String::valueOf);
    }

    private static LongBasedAdaptingStrategy genStrategy(
            ProblemFilterType filterType, Long minT, Long maxT,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        return new LongBasedAdaptingStrategy(
                minT, maxT, useFrom, useTo, useEqualTo
        ) {
            @Override
            public ProblemFilterType handleableFilterType() {
                return filterType;
            }
        };
    }

    private static Long getLongOrNull(Number n) {
        return n == null ? null : n.longValue();
    }

    @Test
    @DisplayName("Long 기반 전략의 comparaotr 가 올바르다.")
    void testComparator() {
        LongBasedAdaptingStrategy strategy = genStrategy(
                null, null, null, true, true, true
        );

        long lower = -1L, median = 0L, higher = 1L;

        Utils.assertComparator(strategy, lower, median, higher);
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#integerBaseToFilterArguments")
    @DisplayName("Long 기반 전략의 toFilter 가 정상 작동한다.")
    void testToFilter(
            ProblemFilterType filterType, Number from, Number to, Number equalTo,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        for (int i = 0; i < 2; i++) {

            boolean testWithThreshold = (i & 0b1) == 0b1;

            Long fromL = getLongOrNull(from);
            Long toL = getLongOrNull(to);
            Long equalToL = getLongOrNull(equalTo);

            Long expectedFrom = !useFrom ? null : fromL;
            Long expectedTo = !useTo ? null : toL;
            Long expectedEqualTo = !useEqualTo ? null : equalToL;

            Long minT, maxT;

            if (testWithThreshold) {
                Comparator<Long> comparator = Long::compare;
                minT = Utils.getMinima(comparator, fromL, toL, equalToL);
                maxT = Utils.getMaxima(comparator, fromL, toL, equalToL);
            } else {
                minT = maxT = null;
            }

            LongBasedAdaptingStrategy strategy = genStrategy(
                    filterType, minT, maxT, useFrom, useTo, useEqualTo
            );

            FilteringRequest request = genReq(filterType, fromL, toL, equalToL);

            Utils.assertToFilterResponseEquality(
                    request, strategy,
                    expectedFrom, expectedTo, expectedEqualTo
            );
        }
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#problemFilterTypes")
    @DisplayName("전략과 요청의 filter type 이 일치하지 않으면 FilterTypeMissMatchException 이 발생한다.")
    void testFilterTypeMismatch(ProblemFilterType given) {
        ProblemFilterType anyOtherFilterType = Utils.getAnyOtherFilterType(given);
        LongBasedAdaptingStrategy strategy = genStrategy(
                anyOtherFilterType, null, null, true, true, true
        );

        FilteringRequest request = genReq(
                given, null, null, null
        );

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(FilterTypeMissMatchException.class);
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#problemFilterTypes")
    @DisplayName("Long 으로 parsing 할 수 없는 요청은 MalformedFilteringRequestException 을 일으킨다.")
    void testMalformedRequest(ProblemFilterType filterType) {
        LongBasedAdaptingStrategy strategy = genStrategy(
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

        long minT = 10L, maxT = 20L;
        LongBasedAdaptingStrategy strategy = genStrategy(
                filterType, minT, maxT, true, true, true
        );

        long validValue = 15L;
        long[] invalidValues = {
                1L, 5L, 9L,
                21L, 25L, 30L
        };
        for (long invalid : invalidValues) {

            var req1 = genReq(filterType, invalid, validValue, validValue);
            var req2 = genReq(filterType, validValue, invalid, validValue);
            var req3 = genReq(filterType, validValue, validValue, invalid);
            var req4 = genReq(filterType, invalid, invalid, validValue);
            var req5 = genReq(filterType, validValue, invalid, invalid);
            var req6 = genReq(filterType, invalid, invalid, invalid);

            var reqs = Arrays.asList(req1, req2, req3, req4, req5, req6);
            for (var req : reqs) {
                assertThatThrownBy(() -> strategy.toFilter(req))
                        .isInstanceOf(IllegalFilterValueException.class);
            }
        }
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#problemFilterTypes")
    @DisplayName("From, To 가 활성화되고 from 이 to 보다 크면 IllegalFilterValueException 가 발생한다.")
    void testIllegalFilterValue2(ProblemFilterType filterType) {
        LongBasedAdaptingStrategy strategy = genStrategy(
                filterType, null, null, true, true, true
        );

        long from = 10L, to = 1L;
        FilteringRequest request = genReq(filterType, from, to, null);

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(IllegalFilterValueException.class);
    }

    @Test
    @DisplayName("허용 값 범위가 올바르지 않으면 class 생성 시 IllegalStateException 이 발생한다.")
    void testIllegalStateException() {
        long minT = 10L, maxT = -10L;

        assertThatThrownBy(() -> genStrategy(
                null, minT, maxT,
                true, true, true
        ))
                .isInstanceOf(IllegalStateException.class);
    }
}