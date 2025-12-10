package org.app.problem.service.strategy;

import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.*;
import org.app.problem.dto.request.*;

public abstract non-sealed class DoubleBasedAdaptingStrategy
        extends AbstractFilterRequestAdaptingStrategy<Double> {

    private final boolean useFrom, useTo, useEqualTo;

    protected DoubleBasedAdaptingStrategy(
            Double minimumThreshold, Double maximumThreshold,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        super(minimumThreshold, maximumThreshold, Double::compare);
        this.useFrom = useFrom;
        this.useTo = useTo;
        this.useEqualTo = useEqualTo;
    }

    @Override
    public final ProblemFilter<Double> toFilter(FilteringRequest request)
            throws MalformedFilteringRequestException, IllegalFilterValueException {

        super.throwExOnFilterTypeMismatch(request.filterType());

        Double from = useFrom ? super.parseDouble(request.from()) : null;
        Double to = useTo ? super.parseDouble(request.to()) : null;
        Double equalTo = useEqualTo ? super.parseDouble(request.equalTo()) : null;

        return super.buildDoubleFilter(from, to, equalTo);
    }
}
