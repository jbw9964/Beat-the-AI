package org.app.problem.service.strategy.order;

import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.springframework.stereotype.*;

@Component
public class NumOfProblemScenariosAscOrderStrategy
        extends AbstractOrderRequestAdaptor {

    public NumOfProblemScenariosAscOrderStrategy() {
        super(true, NullValueOrderHandling.NULL_LAST);
    }

    @Override
    public ProblemOrder toOrder(OrderingRequest request) throws OrderTypeMismatchException {
        super.throwExOnOrderTypeMismatch(request.orderType());

        NumberPath<Integer> numOfScenarioSet = super.QP.numOfTotalScenarios;

        return super.buildProblemOrder(numOfScenarioSet);
    }

    @Override
    public ProblemOrderType handleableOrderType() {
        return ProblemOrderType.NUM_OF_PROBLEM_SCENARIOS_ASC;
    }
}
