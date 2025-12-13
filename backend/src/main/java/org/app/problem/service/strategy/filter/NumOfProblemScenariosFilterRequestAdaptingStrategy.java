package org.app.problem.service.strategy.filter;

import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class NumOfProblemScenariosFilterRequestAdaptingStrategy extends
        AbstractIntegerBasedFilterRequestAdaptor {

    private static final Integer MIN = 1, MAX = null;

    public NumOfProblemScenariosFilterRequestAdaptingStrategy() {
        super(
                MIN, MAX,
                true, true, false
        );
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.NUM_OF_PROBLEM_SCENARIOS;
    }
}
