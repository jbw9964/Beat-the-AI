package org.app.problem.service.strategy.order;

import static org.assertj.core.api.Assertions.*;

import com.querydsl.core.types.*;
import java.util.*;
import lombok.extern.slf4j.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;

@Slf4j
class Utils {

    private static ProblemOrder getProblemOrderFromStrategy(
            OrderRequestAdaptingStrategy strategy
    ) {
        ProblemOrderType handleableOrderType = strategy.handleableOrderType();
        OrderingRequest request = new OrderingRequest(handleableOrderType, null);
        return strategy.toOrder(request);
    }

    public static OrderSpecifier<?> assertProblemOderAndOrderSpecifierNonNullAndGet(
            OrderRequestAdaptingStrategy strategy
    ) {
        ProblemOrder problemOrder = getProblemOrderFromStrategy(strategy);
        assertThat(problemOrder).isNotNull();

        OrderSpecifier<?> orderSpecifier = problemOrder.orderSpecifier();
        assertThat(orderSpecifier).isNotNull();

        return orderSpecifier;
    }

    public static <T> void assertListHasSizeAndSortedAccordingToOrder(
            int expectedSize, List<T> list, Comparator<T> expectedOrder
    ) {
        assertThat(list).isNotNull().hasSize(expectedSize);
        assertThat(list).isSortedAccordingTo(expectedOrder);
    }
}
