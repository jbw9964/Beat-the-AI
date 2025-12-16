package org.app.problem.service;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.app.util.exception.*;
import org.springframework.stereotype.*;

@Component
public class OrderRequestAdaptor {

    private final Map<ProblemOrderType, OrderRequestAdaptingStrategy> strategyMap;

    public OrderRequestAdaptor(List<OrderRequestAdaptingStrategy> strategies) {
        this.strategyMap = strategies.stream().collect(Collectors.toMap(
                OrderRequestAdaptingStrategy::handleableOrderType,
                Function.identity()
        ));
    }

    public List<ProblemOrder> convertToProblemOrderQuery(
            List<OrderingRequest> orderingRequests
    ) {
        // 우선순위 따른 정렬 어케하는게 좋을까?
        // 그냥 dto 에 Comparable 구현해서 sorted()?
        // 아니면 명시적으로 Comparator.comparing(OrderingRequest::priority)?
        return orderingRequests.stream()
                .sorted()
                .map(this::convert)
                .toList();
    }

    private ProblemOrder convert(OrderingRequest orderingRequest) {
        ProblemOrderType orderType = orderingRequest.orderType();

        if (!strategyMap.containsKey(orderType)) {
            throw new NotImplementedException(
                    String.format(
                            "OrderReuqestAdaptor for orderType=%s has not been implemented",
                            orderType
                    ),
                    String.format(
                            "%s 에 대한 정렬 검색은 아직 구현되지 않았습니다.",
                            orderType
                    )
            );
        }

        return strategyMap.get(orderType).toOrder(orderingRequest);
    }
}
