package org.app.problem.service.strategy.order;

import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.springframework.stereotype.*;

@Component
public class NumOfRatingsAscOrderStrategy
        extends AbstractOrderRequestAdaptingStrategy {

    public NumOfRatingsAscOrderStrategy() {
        super(true, NullValueOrderHandling.NULL_LAST);
    }

    @Override
    public ProblemOrder toOrder(OrderingRequest request) throws OrderTypeMismatchException {
        super.throwExOnOrderTypeMismatch(request.orderType());

        NumberPath<Long> numOfTotalRatings =
                super.QPA_TARGET.ratingInfo.numOfTotalRatings;

        return super.buildProblemOrder(numOfTotalRatings);
    }

    @Override
    public ProblemOrderType handleableOrderType() {
        return ProblemOrderType.NUM_OF_RATINGS_ASC;
    }
}
