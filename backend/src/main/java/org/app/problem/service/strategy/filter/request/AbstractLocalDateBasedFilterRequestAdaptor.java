package org.app.problem.service.strategy.filter.request;

import java.time.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.dto.request.*;

public abstract non-sealed class AbstractLocalDateBasedFilterRequestAdaptor
        extends AbstractFilterRequestAdaptor<LocalDate> {

    private final boolean useFrom, useTo, useEqualTo;

    protected AbstractLocalDateBasedFilterRequestAdaptor(
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
            throws FilterTypeMismatchException,
            MalformedFilterRequestException,
            IllegalFilterValueException {

        super.throwExOnFilterTypeMismatch(request.filterType());

        LocalDate from = useFrom ? super.parseLocalDate(request.from()) : null;
        LocalDate to = useTo ? super.parseLocalDate(request.to()) : null;
        LocalDate equalTo = useEqualTo ? super.parseLocalDate(request.equalTo()) : null;

        return super.buildLocalDateFilter(from, to, equalTo);
    }
}
