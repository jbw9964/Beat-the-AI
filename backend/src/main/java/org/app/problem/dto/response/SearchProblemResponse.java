package org.app.problem.dto.response;

import java.util.*;
import java.util.stream.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.*;
import org.app.util.api.*;

public record SearchProblemResponse(
        SimplePageResponse<SimplePublicProblemInfo> pageResponse,
        List<FilteringInfo> appliedFilters,
        List<OrderingInfo> appliedOrders
) {

    public static SearchProblemResponse of(
            SimplePageResponse<SimplePublicProblemInfo> pageResponse,
            List<ProblemFilter<?>> problemFilters,
            List<ProblemOrder> problemOrders
    ) {
        List<FilteringInfo> appliedFilters = problemFilters.stream()
                .map(FilteringInfo::new)
                .toList();

        List<OrderingInfo> appliedOrders = IntStream.range(0, problemOrders.size()).boxed()
                .map(i -> new OrderingInfo(
                        problemOrders.get(i).getOrderType(), i
                ))
                .toList();

        return new SearchProblemResponse(pageResponse, appliedFilters, appliedOrders);
    }
}
