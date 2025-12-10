package org.app.problem.service.strategy.filter;

import java.time.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;

public abstract non-sealed class LocalDateBasedAdaptingStrategy
        extends AbstractFilterRequestAdaptingStrategy<LocalDate> {

    private final boolean useFrom, useTo, useEqualTo;

    protected LocalDateBasedAdaptingStrategy(
            LocalDate minimumThreshold,
            LocalDate maximumThreshold,
            boolean useFrom, boolean useTo, boolean useEqualTo
    ) {
        super(minimumThreshold, maximumThreshold, LocalDate::compareTo);
        this.useFrom = useFrom;
        this.useTo = useTo;
        this.useEqualTo = useEqualTo;
    }

    @Override
    public final ProblemFilter<LocalDate> toFilter(FilteringRequest request)
            throws MalformedFilteringRequestException, IllegalFilterValueException {

        super.throwExOnFilterTypeMismatch(request.filterType());

        LocalDate from = useFrom ? super.parseLocalDate(request.from()) : null;
        LocalDate to = useTo ? super.parseLocalDate(request.to()) : null;
        LocalDate equalTo = useEqualTo ? super.parseLocalDate(request.equalTo()) : null;

        return super.buildLocalDateFilter(from, to, equalTo);
    }
}
