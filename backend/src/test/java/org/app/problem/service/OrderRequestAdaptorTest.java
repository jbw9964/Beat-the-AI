package org.app.problem.service;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.stream.*;
import org.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class OrderRequestAdaptorTest extends IntegrationTestSupport {

    @Autowired
    OrderRequestAdaptor adaptor;

    private static OrderingRequest genReq(ProblemOrderType type, Integer priority) {
        return new OrderingRequest(type, priority);
    }

    @Test
    @DisplayName("제공된 우선순위에 따라 ProblemOrder 가 제공된다.")
    void test() {

        List<OrderingRequest> orderingRequests = List.of(
                genReq(ProblemOrderType.RATING_AVG_ASC, null),
                genReq(ProblemOrderType.CREATED_TIME_ASC, 40),
                genReq(ProblemOrderType.NUM_OF_REWARDS_ASC, 30),
                genReq(ProblemOrderType.NUM_OF_PLAYS_ASC, 20),
                genReq(ProblemOrderType.NUM_OF_PROBLEM_SCENARIOS_ASC, 10),
                genReq(ProblemOrderType.NUM_OF_RATINGS_ASC, 0)
        );

        List<ProblemOrder> response = adaptor.convertToProblemOrderQuery(orderingRequests);

        assertThat(response).isNotNull().hasSize(orderingRequests.size());

        List<ProblemOrderType> expectedIndices = orderingRequests.stream()
                .map(OrderingRequest::orderType)
                .collect(Collectors.toList());

        Collections.reverse(expectedIndices);
        List<ProblemOrderType> givenOrderTypes = response.stream()
                .map(ProblemOrder::getOrderType)
                .toList();

        assertThat(givenOrderTypes).containsExactlyElementsOf(expectedIndices);
    }

}