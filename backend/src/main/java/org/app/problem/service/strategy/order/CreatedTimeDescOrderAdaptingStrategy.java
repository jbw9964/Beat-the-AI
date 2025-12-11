package org.app.problem.service.strategy.order;

import com.querydsl.core.types.dsl.*;
import java.time.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.springframework.stereotype.*;

@Component
public class CreatedTimeDescOrderAdaptingStrategy
        extends AbstractOrderRequestAdaptingStrategy {

    public CreatedTimeDescOrderAdaptingStrategy() {
        super(false, NullValueOrderHandling.NULL_LAST);
    }

    @Override
    public ProblemOrder toOrder(OrderingRequest request) throws OrderTypeMismatchException {
        super.throwExOnOrderTypeMismatch(request.orderType());

        DateTimePath<LocalDateTime> createdAt = super.PROBLEM.createdAt;

        return super.buildProblemOrder(createdAt);
    }

    @Override
    public ProblemOrderType handleableOrderType() {
        return ProblemOrderType.CREATED_TIME_DESC;
    }
}
