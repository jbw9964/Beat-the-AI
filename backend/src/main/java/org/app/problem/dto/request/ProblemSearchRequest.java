package org.app.problem.dto.request;

import jakarta.validation.*;
import java.util.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.domain.search.order.*;
import org.app.util.api.*;

public record ProblemSearchRequest(

        List<@Valid FilteringRequest>
        filteringRequests,

        List<@Valid OrderingRequest>
        orderingRequests,

        @Valid SimplePageRequest pagingRequest
) {

    private static final SimplePageRequest defaultPagingReq
            = new SimplePageRequest(null, null);

    @Override
    public List<FilteringRequest> filteringRequests() {

        if (filteringRequests == null) {
            return Collections.emptyList();
        }

        ProblemFilterType.assertNoDuplicateFilterExists(filteringRequests);

        for (FilteringRequest req : filteringRequests) {
            ProblemFilterType.assertValidParams(req);
        }

        return filteringRequests;
    }

    @Override
    public List<OrderingRequest> orderingRequests() {

        if (orderingRequests == null) {
            return Collections.emptyList();
        }

        ProblemOrderType.assertNoDuplicateOderExists(orderingRequests);

        return orderingRequests;
    }

    @Override
    public SimplePageRequest pagingRequest() {
        return this.pagingRequest == null ?
                defaultPagingReq : this.pagingRequest;
    }
}
