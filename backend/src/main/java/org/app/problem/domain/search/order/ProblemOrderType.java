package org.app.problem.domain.search.order;

import java.util.*;
import java.util.stream.*;
import lombok.*;
import org.app.problem.domain.exception.*;
import org.app.problem.dto.request.*;

@Getter
@RequiredArgsConstructor
public enum ProblemOrderType {

    CREATED_TIME_ASC(1),
    CREATED_TIME_DESC(CREATED_TIME_ASC.orderTypeIndicator),

    NUM_OF_REWARDS_ASC(2),
    NUM_OF_REWARDS_DESC(NUM_OF_REWARDS_ASC.orderTypeIndicator),

    NUM_OF_PLAYS_ASC(3),
    NUM_OF_PLAYS_DESC(NUM_OF_PLAYS_ASC.orderTypeIndicator),

    NUM_OF_PROBLEM_SCENARIOS_ASC(4),
    NUM_OF_PROBLEM_SCENARIOS_DESC(NUM_OF_PROBLEM_SCENARIOS_ASC.orderTypeIndicator),

    NUM_OF_RATINGS_ASC(5),
    NUM_OF_RATINGS_DESC(NUM_OF_RATINGS_ASC.orderTypeIndicator),

    RATING_AVG_ASC(6),
    RATING_AVG_DESC(RATING_AVG_ASC.orderTypeIndicator),
    ;

    private final int orderTypeIndicator;

    public static void assertNoDuplicateOderExists(
            List<OrderingRequest> orderingRequests
    ) {
        Map<Integer, List<ProblemOrderType>> orderGroup = orderingRequests.stream()
                .map(OrderingRequest::orderType)
                .collect(Collectors.groupingBy(ProblemOrderType::getOrderTypeIndicator));

        for (Map.Entry<Integer, List<ProblemOrderType>> entry : orderGroup.entrySet()) {
            List<ProblemOrderType> orders = entry.getValue();

            if (orders.size() >= 2) {
                throw new ImproperOrderTypesException(String.format(
                        "동일한 정렬 기준이 제공되었거나 상반되는 기준이 제공되었습니다: %s",
                        orders
                ));
            }
        }
    }
}
