package org.app.problem.domain.search.order;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.*;
import org.app.problem.domain.exception.*;
import org.app.problem.dto.request.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

class OrderRequestAdaptingStrategyTest extends IntegrationTestSupport {

    @Autowired
    List<OrderRequestAdaptingStrategy> strategyList;

    private static List<ProblemOrderType> getOrderTypesExcept(
            ProblemOrderType except
    ) {
        return Arrays.stream(ProblemOrderType.values())
                .filter(t -> !t.equals(except))
                .toList();
    }

    @Test
    @DisplayName("모든 정렬 타입에 대한 전략이 구비되어있다.")
    void test() {
        Map<ProblemOrderType, OrderRequestAdaptingStrategy> strategyMap = strategyList.stream()
                .collect(Collectors.toMap(
                        OrderRequestAdaptingStrategy::handleableOrderType,
                        Function.identity()
                ));

        for (ProblemOrderType type : ProblemOrderType.values()) {
            assertThat(strategyMap).containsKey(type);

            OrderRequestAdaptingStrategy strategy = strategyMap.get(type);
            assertThat(strategy).isNotNull();
            assertThat(strategy.handleableOrderType()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("전략과 요청의 order type 이 일치하지 않으면 OrderTypeMismatchException 이 발생한다.")
    void testOrderTypeMismatchException() {
        for (OrderRequestAdaptingStrategy strategy : strategyList) {

            ProblemOrderType handleableOrderType = strategy.handleableOrderType();
            List<ProblemOrderType> orderTypes = getOrderTypesExcept(handleableOrderType);

            for (ProblemOrderType orderType : orderTypes) {
                OrderingRequest request = new OrderingRequest(orderType, null);

                assertThatThrownBy(() -> strategy.toOrder(request))
                        .isInstanceOf(OrderTypeMismatchException.class);
            }
        }
    }
}