package org.app.problem.service.strategy.filter;

import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class NumOfRatingsFilterRequestAdaptingStrategy extends
        AbstractLongBasedFilterRequestAdaptor {

    private static final Long MIN = 1L, MAX = null;

    public NumOfRatingsFilterRequestAdaptingStrategy() {
        super(
                MIN, MAX,
                true, true, false
        );
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.NUM_OF_RATINGS;
    }

}
