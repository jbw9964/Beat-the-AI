package org.app.problem.repository;

import static org.assertj.core.api.Assertions.*;

import java.security.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.function.*;
import lombok.extern.slf4j.*;

@Slf4j
public class Utils {

    private static final Random RAND = new SecureRandom();

    public static int getRandom(int minInclusive, int maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("Min must goe than Max");
        }

        return RAND.nextInt(minInclusive, maxInclusive + 1);
    }

    public static long getRandom(long minInclusive, long maxInclusive) {
        if (minInclusive > maxInclusive) {
            throw new IllegalArgumentException("Min must goe than Max");
        }

        return RAND.nextLong(minInclusive, maxInclusive + 1);
    }

    public static LocalDateTime getRandom(LocalDate minDate, LocalDate maxDate) {
        if (minDate.isAfter(maxDate)) {
            throw new IllegalArgumentException("Min must goe than Max");
        }

        LocalDateTime min = minDate.atTime(LocalTime.MIN);
        LocalDateTime max = maxDate.atTime(LocalTime.MAX);

        long secDiff = ChronoUnit.SECONDS.between(min, max);
        long randomSecDiff = RAND.nextLong(0, secDiff + 1);

        return min.plusSeconds(randomSecDiff);
    }

    public static int getRandomMinExclusive(int minExclusive, int maxInclusive) {
        return getRandom(minExclusive + 1, maxInclusive);
    }

    public static long getRandomMinExclusive(long minExclusive, long maxInclusive) {
        return getRandom(minExclusive + 1, maxInclusive);
    }

    public static LocalDateTime getRandomMinExclusive(LocalDate minDateExclusive,
            LocalDate maxDate) {
        return getRandom(minDateExclusive.plusDays(1), maxDate);
    }

    public static int getRandomMaxExclusive(int minInclusive, int maxExclusive) {
        return getRandom(minInclusive, maxExclusive - 1);
    }

    public static long getRandomMaxExclusive(long minInclusive, long maxExclusive) {
        return getRandom(minInclusive, maxExclusive - 1);
    }

    public static LocalDateTime getRandomMaxExclusive(LocalDate minDate,
            LocalDate maxDateExclusive) {
        return getRandom(minDate, maxDateExclusive.minusDays(1));
    }

    public static <T, I> void assertAContainsAllB(
            List<T> a, List<T> b, Function<T, I> identityMapper
    ) {
        assertThat(a).isNotNull();
        assertThat(b).isNotNull();

        assertThat(a.size()).isEqualTo(b.size());

        List<I> aIds = a.stream().map(identityMapper).toList();
        List<I> bIds = b.stream().map(identityMapper).toList();

        assertThat(aIds).containsExactlyInAnyOrderElementsOf(bIds);
    }

    @SafeVarargs
    public static <T, I> void assertADoesNotContainAnyOfOthers(
            List<T> a, Function<T, I> identityMapper,
            List<T>... others
    ) {
        assertThat(a).isNotNull();

        List<I> aIds = a.stream().map(identityMapper).toList();

        for (List<T> other : others) {
            List<I> otherIds = other.stream().map(identityMapper).toList();

            assertThat(aIds).doesNotContainAnyElementsOf(otherIds);
        }
    }
}
