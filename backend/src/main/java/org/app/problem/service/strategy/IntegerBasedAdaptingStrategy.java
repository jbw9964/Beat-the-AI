package org.app.problem.service.strategy;

import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.*;
import org.app.problem.dto.request.*;

public abstract non-sealed class IntegerBasedAdaptingStrategy
        extends AbstractFilterRequestAdaptingStrategy<Integer> {

    private final boolean useFrom, useTo, useEqualTo;

    protected IntegerBasedAdaptingStrategy(
            Integer minimumThreshold, Integer maximumThreshold,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        super(minimumThreshold, maximumThreshold, Integer::compare);
        this.useFrom = useFrom;
        this.useTo = useTo;
        this.useEqualTo = useEqualTo;
    }

    @Override
    public final ProblemFilter<Integer> toFilter(FilteringRequest request)
            throws MalformedFilteringRequestException, IllegalFilterValueException {

        super.throwExOnFilterTypeMismatch(request.filterType());

        Integer from = useFrom ? super.parseInteger(request.from()) : null;
        Integer to = useTo ? super.parseInteger(request.to()) : null;
        Integer equalTo = useEqualTo ? super.parseInteger(request.equalTo()) : null;

        return super.buildIntegerFilter(from, to, equalTo);
    }

}
