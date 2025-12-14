package org.app.problem.repository.strategy;

import static org.assertj.core.api.Assertions.*;

import java.security.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.function.*;
import org.app.problem.domain.search.filter.*;

class Utils {

    private static final Random RAND = new SecureRandom();

    public static List<Long> createRandomLongsInclusive(
            int size, long min, long max
    ) {
        if (size < 2) {
            throw new IllegalArgumentException("Size must goe than 2");
        }

        if (min > max) {
            throw new IllegalArgumentException("Min must goe than Max");
        }

        List<Long> result = new ArrayList<>(size);
        result.add(min);
        result.add(max);

        for (int i = 0; i < size - 2; i++) {
            long rand = RAND.nextLong(min, max + 1);
            result.add(rand);
        }

        if (result.size() != size) {
            throw new AssertionError();
        }

        Collections.shuffle(result);

        return result;
    }

    public static List<Integer> creatRandomIntsInclusive(
            int size, int min, int max
    ) {
        if (size < 2) {
            throw new IllegalArgumentException("Size must goe than 2");
        }

        if (min > max) {
            throw new IllegalArgumentException("Min must goe than Max");
        }

        List<Integer> result = new ArrayList<>(size);
        result.add(min);
        result.add(max);

        for (int i = 0; i < size - 2; i++) {
            int rand = RAND.nextInt(min, max + 1);
            result.add(rand);
        }

        if (result.size() != size) {
            throw new AssertionError();
        }

        Collections.shuffle(result);

        return result;
    }

    public static List<LocalDate> createRandomLocalDatesInclusive(
            int size, LocalDate min, LocalDate max
    ) {
        if (size < 2) {
            throw new IllegalArgumentException("Size must goe than 2");
        }

        if (min.isAfter(max)) {
            throw new IllegalArgumentException("Min must goe than Max");
        }

        List<LocalDate> result = new ArrayList<>(size);
        result.add(min);
        result.add(max);

        long dayDiff = ChronoUnit.DAYS.between(min, max);

        for (int i = 0; i < size - 2; i++) {
            long randDayDiff = RAND.nextLong(0, dayDiff + 1);
            LocalDate rand = min.plusDays(randDayDiff);
            result.add(rand);
        }

        if (result.size() != size) {
            throw new AssertionError();
        }

        Collections.shuffle(result);

        return result;
    }

    public static <T> ProblemFilter<T> genProblemFilter(
            FilterClausesBuilderStrategy strategy, T from, T to, T equalTo
    ) {
        return new ProblemFilter<>() {
            @Override
            public T getFrom() {
                return from;
            }

            @Override
            public T getTo() {
                return to;
            }

            @Override
            public T getEqualTo() {
                return equalTo;
            }

            @Override
            public ProblemFilterType getFilterType() {
                return strategy.handleableFilterType();
            }
        };
    }

    public static <T, I> void assertAContainsAllB(
            List<T> a, List<T> b,
            Function<T, I> identityMapper
    ) {
        assertThat(a).isNotNull();
        assertThat(b).isNotNull();

        assertThat(b.size()).isEqualTo(a.size());

        List<I> aIdentities = a.stream().map(identityMapper).toList();
        List<I> bIdentities = b.stream().map(identityMapper).toList();

        assertThat(aIdentities).containsExactlyInAnyOrderElementsOf(
                bIdentities
        );
    }

    public static <T, I> void assertADoesNotContainsAnyB(
            List<T> a, List<T> b,
            Function<T, I> identityMapper
    ) {
        assertThat(a).isNotNull();
        assertThat(b).isNotNull();

        List<I> aIdentities = a.stream().map(identityMapper).toList();
        List<I> bIdentities = b.stream().map(identityMapper).toList();

        assertThat(aIdentities).doesNotContainAnyElementsOf(
                bIdentities
        );
    }

    public static <T> void assertAllElementsSatisfies(
            List<T> list, Predicate<T> satisfies
    ) {
        assertThat(list).isNotNull();
        assertThat(list).allSatisfy(satisfies::test);
    }
}
