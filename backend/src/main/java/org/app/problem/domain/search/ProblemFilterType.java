package org.app.problem.domain.search;

import org.app.problem.dto.request.*;
import org.app.util.exception.*;

public enum ProblemFilterType {
    CREATED_TIME,
    CREATED_USER,

    NUM_OF_REWARDS,
    NUM_OF_PLAYS,
    NUM_OF_PROBLEM_SCENARIOS,
    NUM_OF_RATINGS,

    RATING_AVG;

    public static void assertValidParams(FilteringRequest filteringRequest)
            throws BadRequestException {
        ProblemFilterType type = filteringRequest.filterType();
        String from = filteringRequest.from();
        String to = filteringRequest.to();
        String equalTo = filteringRequest.equalTo();

        switch (type) {
            case CREATED_TIME:
            case NUM_OF_REWARDS:
            case NUM_OF_PLAYS:
            case NUM_OF_PROBLEM_SCENARIOS:
            case NUM_OF_RATINGS:
            case RATING_AVG:
                if (
                        (from == null || from.isEmpty()) &&
                        (to == null || to.isEmpty())
                ) {
                    throw new BadRequestException(String.format(
                            "[type=%s] 필터링은 from, to 둘 중 하나는 제공되어야 합니다. (from=null,to=null)",
                            type
                    ));
                }
                break;

            case CREATED_USER:
                if (
                        equalTo == null || equalTo.isEmpty()
                ) {
                    throw new BadRequestException(String.format(
                            "[type=%s] 필터링은 equalTo 속성이 제공되어야 합니다. (equalTo=null)",
                            type
                    ));
                }
                break;

            default:
                throw new RuntimeException("Unexpected filter type: " + type);
        }
    }
}
