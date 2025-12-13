package org.app.problem.service.strategy.filter;

import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class RatingAvgFilterRequestAdaptingStrategy extends
        AbstractDoubleBasedFilterRueqestAdaptor {

    private static final Double MIN = 0.d, MAX = 5.d;

    public RatingAvgFilterRequestAdaptingStrategy() {
        super(
                MIN, MAX,
                true, true, false
        );
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.RATING_AVG;
    }

}
