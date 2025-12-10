package org.app.problem.domain.search.filter;

import lombok.*;
import lombok.experimental.*;
import org.app.problem.dto.request.*;
import org.app.util.exception.*;

@RequiredArgsConstructor
public enum ProblemFilterType {
    CREATED_TIME(true, true, false),
    CREATED_USER(false, false, true),

    NUM_OF_REWARDS(true, true, false),
    NUM_OF_PLAYS(true, true, false),
    NUM_OF_PROBLEM_SCENARIOS(true, true, false),
    NUM_OF_RATINGS(true, true, false),

    RATING_AVG(true, true, false),
    ;

    @Getter
    @Accessors(fluent = true, chain = false)
    private final boolean useFrom, useTo, useEqualTo;

    public static void assertValidParams(FilteringRequest filteringRequest)
            throws BadRequestException {
        ProblemFilterType type = filteringRequest.filterType();
        String from = filteringRequest.from();
        String to = filteringRequest.to();
        String equalTo = filteringRequest.equalTo();

        boolean useBoundedSearchRange = type.useFrom() && type.useTo();
        boolean useExactValueSearch = type.useEqualTo();

        boolean noSearchRangeProvided =
                (from == null || from.isEmpty()) && (to == null || to.isEmpty());
        boolean noExactValueProvided = equalTo == null || equalTo.isEmpty();

        if (useBoundedSearchRange && noSearchRangeProvided) {
            throw new BadRequestException(String.format(
                    "[type=%s] 필터링은 from, to 둘 중 하나는 제공되어야 합니다. (from=%s,to=%s)",
                    type, from, to
            ));
        }

        if (useExactValueSearch && noExactValueProvided) {
            throw new BadRequestException(String.format(
                    "[type=%s] 필터링은 equalTo 속성이 제공되어야 합니다. (equalTo=%s)",
                    type, equalTo
            ));
        }
    }
}
