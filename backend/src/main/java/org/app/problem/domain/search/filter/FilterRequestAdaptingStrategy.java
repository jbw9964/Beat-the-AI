package org.app.problem.domain.search.filter;

import org.app.problem.domain.exception.*;
import org.app.problem.dto.request.*;

public interface FilterRequestAdaptingStrategy<T> {

    ProblemFilter<T> toFilter(FilteringRequest request)
            throws FilterTypeMismatchException,
            MalformedFilteringRequestException,
            IllegalFilterValueException;

    ProblemFilterType handleableFilterType();

}
