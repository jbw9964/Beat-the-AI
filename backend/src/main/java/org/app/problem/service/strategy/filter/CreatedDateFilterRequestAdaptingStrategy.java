package org.app.problem.service.strategy.filter;

import java.time.*;
import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class CreatedDateFilterRequestAdaptingStrategy extends
        AbstractLocalDateBasedFilterRequestAdaptor {

    private static final LocalDate MIN = null,
            MAX = null;

    public CreatedDateFilterRequestAdaptingStrategy() {
        super(
                MIN, MAX,
                true, true, false
        );
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.CREATED_DATE;
    }
}
