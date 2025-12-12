package org.app.problem.service.strategy.filter.request;

import java.time.*;
import java.time.format.*;
import java.util.*;
import lombok.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;

@Getter
@SuppressWarnings({"SameParameterValue", "DuplicatedCode"})
public abstract sealed class AbstractFilterRequestAdaptor<T>
        implements FilterRequestAdaptingStrategy<T>
        permits AbstractDoubleBasedFilterRueqestAdaptor,
        AbstractIntegerBasedFilterRequestAdaptor,
        AbstractLocalDateBasedFilterRequestAdaptor,
        AbstractLongBasedFilterRequestAdaptor {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd";
    public static final DateTimeFormatter DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    private final T minimumThreshold;
    private final T maximumThreshold;
    private final Comparator<T> comparator;

    protected AbstractFilterRequestAdaptor(
            T minimumThreshold, T maximumThreshold,
            @NonNull Comparator<T> comparator
    ) {
        this.minimumThreshold = minimumThreshold;
        this.maximumThreshold = maximumThreshold;
        this.comparator = comparator;

        if (
                minimumThreshold != null && maximumThreshold != null &&
                comparator.compare(minimumThreshold, maximumThreshold) > 0
        ) {
            throw new IllegalStateException(String.format(
                    "getMinimumThreshold() must be less or equal to getMaximumThreshold(): "
                    + "[getMinimumThreshold()=%s, getMaximumThreshold()=%s]",
                    minimumThreshold, maximumThreshold
            ));
        }
    }

    protected final void throwExOnFilterTypeMismatch(ProblemFilterType given) {
        ProblemFilterType handleableFilterType = this.handleableFilterType();

        if (!given.equals(handleableFilterType)) {
            throw new FilterTypeMismatchException(String.format(
                    "Expected to get [%s]-typed filtering request, "
                    + "but encountered [%s] type on adaptor: %s",
                    handleableFilterType, given, this.getClass().getSimpleName()
            ));
        }
    }

    protected final Long parseLong(String given) throws MalformedFilterRequestException {
        if (given == null) {
            return null;
        }

        long result;
        try {
            result = Long.parseLong(given);
        } catch (NumberFormatException e) {
            throw buildEx(given, Long.class, e);
        }
        return result;
    }

    protected final Integer parseInteger(String given) throws MalformedFilterRequestException {
        if (given == null) {
            return null;
        }

        int result;
        try {
            result = Integer.parseInt(given);
        } catch (NumberFormatException e) {
            throw buildEx(given, Integer.class, e);
        }
        return result;
    }

    protected final Double parseDouble(String given) throws MalformedFilterRequestException {
        if (given == null) {
            return null;
        }

        double result;
        try {
            result = Double.parseDouble(given);
        } catch (NumberFormatException e) {
            throw buildEx(given, Double.class, e);
        }
        return result;
    }

    protected final LocalDate parseLocalDate(String given)
            throws MalformedFilterRequestException {
        if (given == null) {
            return null;
        }

        LocalDate result;
        try {
            result = LocalDate.parse(given, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw buildEx(given, LocalDateTime.class, e);
        }
        return result;
    }

    protected final ProblemFilter<Integer> buildIntegerFilter(
            Integer from, Integer to, Integer equalTo
    ) throws IllegalFilterValueException {
        ProblemFilterType filterType = this.handleableFilterType();

        //noinspection unchecked
        if (
                this.outOfThresholdRange((T) from) ||
                this.outOfThresholdRange((T) to) ||
                this.outOfThresholdRange((T) equalTo)
        ) {
            throw buildEx(filterType, from, to);
        }

        if (
                from != null && to != null &&
                from > to
        ) {
            throw new IllegalFilterValueException(String.format(
                    "[type=%s] 필터링의 from 은 to 보다 작거나 같아야 합니다: (from=%d,to=%d)",
                    filterType, from, to
            ));
        }

        return new ProblemFilter<>() {

            @Override
            public Integer getFrom() {
                return from;
            }

            @Override
            public Integer getTo() {
                return to;
            }

            @Override
            public Integer getEqualTo() {
                return equalTo;
            }

            @Override
            public ProblemFilterType getFilterType() {
                return filterType;
            }
        };
    }

    protected final ProblemFilter<Long> buildLongFilter(
            Long from, Long to, Long equalTo
    ) throws IllegalFilterValueException {
        ProblemFilterType filterType = this.handleableFilterType();

        //noinspection unchecked
        if (
                this.outOfThresholdRange((T) from) ||
                this.outOfThresholdRange((T) to) ||
                this.outOfThresholdRange((T) equalTo)
        ) {
            throw buildEx(filterType, from, to);
        }

        if (
                from != null && to != null &&
                from > to
        ) {
            throw new IllegalFilterValueException(String.format(
                    "[type=%s] 필터링의 from 은 to 보다 작거나 같아야 합니다: (from=%d,to=%d)",
                    filterType, from, to
            ));
        }

        return new ProblemFilter<>() {

            @Override
            public Long getFrom() {
                return from;
            }

            @Override
            public Long getTo() {
                return to;
            }

            @Override
            public Long getEqualTo() {
                return equalTo;
            }

            @Override
            public ProblemFilterType getFilterType() {
                return filterType;
            }
        };
    }

    protected final ProblemFilter<LocalDate> buildLocalDateFilter(
            LocalDate from, LocalDate to, LocalDate equalTo
    ) throws IllegalFilterValueException {
        ProblemFilterType filterType = this.handleableFilterType();

        //noinspection unchecked
        if (
                this.outOfThresholdRange((T) from) ||
                this.outOfThresholdRange((T) to) ||
                this.outOfThresholdRange((T) equalTo)
        ) {
            throw buildEx(filterType, from, to);
        }

        if (
                from != null && to != null &&
                from.isAfter(to)
        ) {
            throw new IllegalFilterValueException(String.format(
                    "[type=%s] 필터링의 from 은 to 보다 더 이르거나 같은 날짜여야 합니다: (from=%s,to=%s)",
                    filterType, from, to
            ));
        }

        return new ProblemFilter<>() {

            @Override
            public LocalDate getFrom() {
                return from;
            }

            @Override
            public LocalDate getTo() {
                return to;
            }

            @Override
            public LocalDate getEqualTo() {
                return equalTo;
            }

            @Override
            public ProblemFilterType getFilterType() {
                return filterType;
            }
        };
    }

    protected final ProblemFilter<Double> buildDoubleFilter(
            Double from, Double to, Double equalTo
    ) throws IllegalFilterValueException {
        ProblemFilterType filterType = this.handleableFilterType();

        //noinspection unchecked
        if (
                this.outOfThresholdRange((T) from) ||
                this.outOfThresholdRange((T) to) ||
                this.outOfThresholdRange((T) equalTo)
        ) {
            throw buildEx(filterType, from, to);
        }

        if (
                from != null && to != null &&
                from > to
        ) {
            throw new IllegalFilterValueException(String.format(
                    "[type=%s] 필터링의 from 은 to 보다 작거나 같아야 합니다: (from=%.1f,to=%.1f)",
                    filterType, from, to
            ));
        }

        return new ProblemFilter<>() {
            @Override
            public Double getFrom() {
                return from;
            }

            @Override
            public Double getTo() {
                return to;
            }

            @Override
            public Double getEqualTo() {
                return equalTo;
            }

            @Override
            public ProblemFilterType getFilterType() {
                return filterType;
            }
        };
    }

    private boolean outOfThresholdRange(T value) {
        if (value == null) {
            return false;
        }

        T minT = minimumThreshold;
        T maxT = maximumThreshold;
        boolean inRange = true;

        if (minT != null) {
            inRange &= comparator.compare(minT, value) <= 0;
        }

        if (maxT != null) {
            inRange &= comparator.compare(value, maxT) <= 0;
        }

        return !inRange;
    }

    private MalformedFilterRequestException buildEx(
            String given, Class<?> targetClass, RuntimeException e
    ) {
        return new MalformedFilterRequestException(String.format(
                "주어진 값 (%s) 을 [%s] 로 변환하는데 실패했습니다: %s",
                given, targetClass.getSimpleName(), e.getMessage()
        ));
    }

    private IllegalFilterValueException buildEx(
            ProblemFilterType filterType, Object from, Object to
    ) {
        T minT = this.minimumThreshold;
        T maxT = this.maximumThreshold;

        return new IllegalFilterValueException(String.format(
                "[type=%s] 필터링의 값은 [%s::%s] 범위 이내의 값 이어야 합니다. (from=%s,to=%s)",
                filterType,
                minT != null ? minT : "~",
                maxT != null ? maxT : "~",
                from, to
        ));
    }
}
