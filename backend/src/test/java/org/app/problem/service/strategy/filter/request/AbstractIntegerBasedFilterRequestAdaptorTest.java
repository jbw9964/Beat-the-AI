package org.app.problem.service.strategy.filter.request;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class AbstractIntegerBasedFilterRequestAdaptorTest {

    private static FilteringRequest genReq(
            ProblemFilterType filterType, Integer from, Integer to, Integer equalTo
    ) {
        return Utils.genRequest(filterType, from, to, equalTo, String::valueOf);
    }

    private static AbstractIntegerBasedFilterRequestAdaptor genStrategy(
            ProblemFilterType filterType, Integer minT, Integer maxT,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        return new AbstractIntegerBasedFilterRequestAdaptor(
                minT, maxT, useFrom, useTo, useEqualTo
        ) {
            @Override
            public ProblemFilterType handleableFilterType() {
                return filterType;
            }
        };
    }

    private static Integer getIntOrNull(Number n) {
        return n == null ? null : n.intValue();
    }

    @Test
    @DisplayName("Integer 기반 전략의 comparaotr 가 올바르다.")
    void testComparator() {
        AbstractIntegerBasedFilterRequestAdaptor strategy = genStrategy(
                null, null, null, true, true, true
        );

        int lower = -1, median = 0, higher = 1;

        Utils.assertComparator(strategy, lower, median, higher);
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.request."
                  + "Utils#integerBaseToFilterArguments")
    @DisplayName("Integer 기반 전략의 toFilter 가 정상 작동한다.")
    void testToFilter(
            ProblemFilterType filterType, Number from, Number to, Number equalTo,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        for (int i = 0; i < 2; i++) {

            boolean testWithThreshold = (i & 0b1) == 0b1;

            Integer fromI = getIntOrNull(from);
            Integer toI = getIntOrNull(to);
            Integer euqalToI = getIntOrNull(equalTo);

            Integer expectedFrom = !useFrom ? null : fromI;
            Integer expectedTo = !useTo ? null : toI;
            Integer expectedEqualTo = !useEqualTo ? null : euqalToI;

            Integer minT, maxT;

            if (testWithThreshold) {
                Comparator<Integer> comparator = Integer::compare;
                minT = Utils.getMinima(comparator, fromI, toI, euqalToI);
                maxT = Utils.getMaxima(comparator, fromI, toI, euqalToI);
            } else {
                minT = maxT = null;
            }

            AbstractIntegerBasedFilterRequestAdaptor strategy = genStrategy(
                    filterType, minT, maxT, useFrom, useTo, useEqualTo
            );

            FilteringRequest request = genReq(filterType, fromI, toI, euqalToI);

            Utils.assertToFilterResponseEquality(
                    request, strategy,
                    expectedFrom, expectedTo, expectedEqualTo
            );
        }
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.request."
                  + "Utils#problemFilterTypes")
    @DisplayName("허용 범위 밖 값들이 제공되면 IllegalFilterValueException 이 발생한다.")
    void testIllegalFilterValue1(ProblemFilterType filterType) {

        int minT = 10, maxT = 20;
        AbstractIntegerBasedFilterRequestAdaptor strategy = genStrategy(
                filterType, minT, maxT, true, true, true
        );

        int validValue = 15;
        int[] invalidValues = {
                1, 5, 9,
                21, 25, 30
        };
        for (int invalid : invalidValues) {

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
    @MethodSource("org.app.problem.service.strategy.filter.request."
                  + "Utils#problemFilterTypes")
    @DisplayName("From, To 가 활성화되고 from 이 to 보다 크면 IllegalFilterValueException 가 발생한다.")
    void testIllegalFilterValue2(ProblemFilterType filterType) {
        AbstractIntegerBasedFilterRequestAdaptor strategy = genStrategy(
                filterType, null, null, true, true, true
        );

        int from = 10, to = 1;
        FilteringRequest request = genReq(filterType, from, to, null);

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(IllegalFilterValueException.class);
    }

    @Test
    @DisplayName("허용 값 범위가 올바르지 않으면 class 생성 시 IllegalStateException 이 발생한다.")
    void testIllegalStateException() {
        int minT = 10, maxT = -10;

        assertThatThrownBy(() -> genStrategy(
                null, minT, maxT,
                true, true, true
        ))
                .isInstanceOf(IllegalStateException.class);
    }
}