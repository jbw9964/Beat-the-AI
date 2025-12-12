package org.app.problem.service.strategy.filter;

import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;

public abstract non-sealed class AbstractLongBasedFilterRequestAdaptor
        extends AbstractFilterRequestAdaptor<Long> {

    private final boolean useFrom, useTo, useEqual;

    protected AbstractLongBasedFilterRequestAdaptor(
            Long minimumThreshold, Long maximumThreshold,
            boolean useFrom, boolean useTo, boolean useEqual
    ) {
        super(minimumThreshold, maximumThreshold, Long::compare);
        this.useFrom = useFrom;
        this.useTo = useTo;
        this.useEqual = useEqual;
    }

    @Override
    public final ProblemFilter<Long> toFilter(FilteringRequest request)
            throws FilterTypeMismatchException,
            MalformedFilterRequestException,
            IllegalFilterValueException {

        super.throwExOnFilterTypeMismatch(request.filterType());

        Long from = useFrom ? super.parseLong(request.from()) : null;
        Long to = useTo ? super.parseLong(request.to()) : null;
        Long eqaulTo = useEqual ? super.parseLong(request.equalTo()) : null;

        return super.buildLongFilter(from, to, eqaulTo);
    }
}
