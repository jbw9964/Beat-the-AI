package org.app.problem.service.strategy.order;

import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.springframework.stereotype.*;

@Component
public class NumOfRewardsAscOrderStrategy
        extends AbstractOrderRequestAdaptingStrategy {

    public NumOfRewardsAscOrderStrategy() {
        super(true, NullValueOrderHandling.NULL_LAST);
    }

    @Override
    public ProblemOrder toOrder(OrderingRequest request) throws OrderTypeMismatchException {
        super.throwExOnOrderTypeMismatch(request.orderType());

        NumberPath<Integer> numOfRewardSet =
                super.QPA_TARGET.problemInfo.numOfRewardSet;

        return super.buildProblemOrder(numOfRewardSet);
    }

    @Override
    public ProblemOrderType handleableOrderType() {
        return ProblemOrderType.NUM_OF_REWARDS_ASC;
    }
}
