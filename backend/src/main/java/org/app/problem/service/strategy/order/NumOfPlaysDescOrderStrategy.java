package org.app.problem.service.strategy.order;

import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.springframework.stereotype.*;

@Component
public class NumOfPlaysDescOrderStrategy
        extends AbstractOrderRequestAdaptingStrategy {

    public NumOfPlaysDescOrderStrategy() {
        super(false, NullValueOrderHandling.NULL_LAST);
    }

    @Override
    public ProblemOrder toOrder(OrderingRequest request) throws OrderTypeMismatchException {
        super.throwExOnOrderTypeMismatch(request.orderType());

        NumberPath<Long> numOfTotalPlays =
                super.QPA_TARGET.playInfo.numOfTotalPlays;

        return super.buildProblemOrder(numOfTotalPlays);
    }

    @Override
    public ProblemOrderType handleableOrderType() {
        return ProblemOrderType.NUM_OF_PLAYS_DESC;
    }
}
