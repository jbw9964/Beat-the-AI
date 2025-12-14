package org.app.problem.service.strategy.filter;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class AbstractDoubleBasedFilterRueqestAdaptorTest {

    private static FilteringRequest genReq(
            ProblemFilterType filterType, Double from, Double to, Double equalTo
    ) {
        return Utils.genRequest(filterType, from, to, equalTo, String::valueOf);
    }

    private static AbstractDoubleBasedFilterRueqestAdaptor genStrategy(
            ProblemFilterType filterType, Double minT, Double maxT,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        return new AbstractDoubleBasedFilterRueqestAdaptor(
                minT, maxT, useFrom, useTo, useEqualTo
        ) {
            @Override
            public ProblemFilterType handleableFilterType() {
                return filterType;
            }
        };
    }

    private static Double getDoubleOrNull(Number n) {
        return n == null ? null : n.doubleValue();
    }

    @Test
    @DisplayName("Double 기반 전략의 comparaotr 가 올바르다.")
    void testComparator() {
        AbstractDoubleBasedFilterRueqestAdaptor strategy = genStrategy(
                null, null, null, true, true, true
        );

        double lower = -1.d, median = 0.d, higher = 1.d;

        Utils.assertComparator(strategy, lower, median, higher);
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#integerBaseToFilterArguments")
    @DisplayName("Double 기반 전략의 toFilter 가 정상 작동한다.")
    void testToFilter(
            ProblemFilterType filterType, Number from, Number to, Number equalTo,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        for (int i = 0; i < 2; i++) {

            boolean testWithThreshold = (i & 0b1) == 0b1;

            Double fromD = getDoubleOrNull(from);
            Double toD = getDoubleOrNull(to);
            Double equalToD = getDoubleOrNull(equalTo);

            Double minT, maxT;

            if (testWithThreshold) {
                Comparator<Double> comparator = Double::compare;
                minT = Utils.getMinima(comparator, fromD, toD, equalToD);
                maxT = Utils.getMaxima(comparator, fromD, toD, equalToD);
            } else {
                minT = maxT = null;
            }

            AbstractDoubleBasedFilterRueqestAdaptor strategy = genStrategy(
                    filterType, minT, maxT, useFrom, useTo, useEqualTo
            );

            Double expectedFrom = !useFrom ? null : fromD;
            Double expectedTo = !useTo ? null : toD;
            Double expectedEqualTo = !useEqualTo ? null : equalToD;

            FilteringRequest request = genReq(filterType, fromD, toD, equalToD);

            Utils.assertToFilterResponseEquality(
                    request, strategy,
                    expectedFrom, expectedTo, expectedEqualTo
            );
        }
    }

    @ParameterizedTest
    @MethodSource("org.app.problem.service.strategy.filter.Utils#problemFilterTypes")
    @DisplayName("허용 범위 밖 값들이 제공되면 IllegalFilterValueException 이 발생한다.")
    void testIllegalFilterValue1(ProblemFilterType filterType) {

        double minT = 10.d, maxT = 20.d;
        AbstractDoubleBasedFilterRueqestAdaptor strategy = genStrategy(
                filterType, minT, maxT, true, true, true
        );

        double validValue = 15.d;
        double[] invalidValues = {
                1.d, 5.d, 9.d,
                21.d, 25.d, 30.d
        };
        for (double invalid : invalidValues) {

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
        AbstractDoubleBasedFilterRueqestAdaptor strategy = genStrategy(
                filterType, null, null, true, true, true
        );

        double from = 10.d, to = 1.d;
        FilteringRequest request = genReq(filterType, from, to, null);

        assertThatThrownBy(() -> strategy.toFilter(request))
                .isInstanceOf(IllegalFilterValueException.class);
    }

    @Test
    @DisplayName("허용 값 범위가 올바르지 않으면 class 생성 시 IllegalStateException 이 발생한다.")
    void testIllegalStateException() {
        double minT = 10.d, maxT = -10.d;

        assertThatThrownBy(() -> genStrategy(
                null, minT, maxT,
                true, true, true
        ))
                .isInstanceOf(IllegalStateException.class);
    }
}