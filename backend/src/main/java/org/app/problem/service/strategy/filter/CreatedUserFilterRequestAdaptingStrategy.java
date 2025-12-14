package org.app.problem.service.strategy.filter;

import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class CreatedUserFilterRequestAdaptingStrategy
        extends AbstractLongBasedFilterRequestAdaptor {

    private static final Long MIN = 0L, MAX = null;

    public CreatedUserFilterRequestAdaptingStrategy() {
        super(
                MIN, MAX,
                false, false, true
        );
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.CREATED_USER;
    }

}
