package org.app.problem.service.strategy;

import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.*;
import org.app.problem.dto.request.*;

public abstract non-sealed class LongBasedAdaptingStrategy
        extends AbstractFilterRequestAdaptingStrategy<Long> {

    private final boolean useFrom, useTo, useEqual;

    protected LongBasedAdaptingStrategy(
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
            throws MalformedFilteringRequestException, IllegalFilterValueException {

        super.throwExOnFilterTypeMismatch(request.filterType());

        Long from = useFrom ? super.parseLong(request.from()) : null;
        Long to = useTo ? super.parseLong(request.to()) : null;
        Long eqaulTo = useEqual ? super.parseLong(request.equalTo()) : null;

        return super.buildLongFilter(from, to, eqaulTo);
    }
}
