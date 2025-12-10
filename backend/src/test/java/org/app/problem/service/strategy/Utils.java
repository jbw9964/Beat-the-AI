package org.app.problem.service.strategy;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.app.problem.domain.search.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.params.provider.*;

class Utils {

    public static <T> FilteringRequest genRequest(
            ProblemFilterType filterType, T from, T to, T equalTo,
            Function<T, String> paramMapper
    ) {
        String fromStr = from == null ? null :
                paramMapper.apply(from);
        String toStr = to == null ? null :
                paramMapper.apply(to);
        String equalToStr = equalTo == null ? null :
                paramMapper.apply(equalTo);

        return new FilteringRequest(filterType, fromStr, toStr, equalToStr);
    }

    @SafeVarargs
    public static <T> T getMinima(
            Comparator<T> comparator, T... values
    ) {
        if (values == null || values.length == 0) {
            return null;
        }

        T minima = values[0];
        for (int i = 1; i < values.length; i++) {

            T val = values[i];
            if (
                    minima == null ||
                    (val != null && comparator.compare(minima, val) > 0)
            ) {
                minima = val;
            }
        }

        return minima;
    }

    @SafeVarargs
    public static <T> T getMaxima(
            Comparator<T> comparator, T... values
    ) {
        if (values == null || values.length == 0) {
            return null;
        }

        T maxima = values[0];
        for (int i = 1; i < values.length; i++) {

            T val = values[i];
            if (
                    maxima == null ||
                    (val != null && comparator.compare(maxima, val) < 0)
            ) {
                maxima = val;
            }
        }
        return maxima;
    }

    public static ProblemFilterType getAnyOtherFilterType(ProblemFilterType given) {
        return Arrays.stream(ProblemFilterType.values())
                .filter(pt -> !pt.equals(given))
                .findAny()
                .orElseThrow(AssertionError::new);
    }

    public static Stream<Arguments> integerBaseToFilterArguments() {
        return Stream.of(
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        0, 1, 2,
                        true, false, false
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        null, 1, 2,
                        true, false, false
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        3, 4, 5,
                        false, true, false
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        3, null, 5,
                        false, true, false
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        6, 7, 8,
                        false, false, true
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        6, 7, null,
                        false, false, true
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        9, 10, 11,
                        true, true, false
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        null, 10, 11,
                        true, true, false
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        9, null, 11,
                        true, true, false
                ),
                Arguments.of(
                        ProblemFilterType.NUM_OF_PLAYS,
                        null, null, 11,
                        true, true, false
                )
        );
    }

    public static Stream<Arguments> problemFilterTypes() {
        return Stream.of(
                Arrays.stream(ProblemFilterType.values())
                        .map(Arguments::of)
                        .toArray(Arguments[]::new)
        );
    }

    @SuppressWarnings("EqualsWithItself")
    public static <T> void assertComparator(
            AbstractFilterRequestAdaptingStrategy<T> strategy,
            T lower, T median, T higher
    ) {
        Comparator<T> comparator = strategy.getComparator();
        assertThat(comparator).isNotNull();

        assertThat(comparator.compare(lower, median)).isLessThan(0);
        assertThat(comparator.compare(lower, higher)).isLessThan(0);
        assertThat(comparator.compare(median, higher)).isLessThan(0);

        assertThat(comparator.compare(higher, lower)).isGreaterThan(0);
        assertThat(comparator.compare(higher, median)).isGreaterThan(0);
        assertThat(comparator.compare(median, lower)).isGreaterThan(0);

        assertThat(comparator.compare(lower, lower)).isZero();
        assertThat(comparator.compare(median, median)).isZero();
        assertThat(comparator.compare(higher, higher)).isZero();
    }

    public static <T> void assertToFilterResponseEquality(
            FilteringRequest request, AbstractFilterRequestAdaptingStrategy<T> strategy,
            T expectedFrom, T expectedTo, T expectedEqualTo
    ) {
        ProblemFilter<T> response = strategy.toFilter(request);

        assertThat(response).isNotNull();
        assertThat(response.getFrom()).isEqualTo(expectedFrom);
        assertThat(response.getTo()).isEqualTo(expectedTo);
        assertThat(response.getEqualTo()).isEqualTo(expectedEqualTo);
    }
}
