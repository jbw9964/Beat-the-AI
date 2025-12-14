package org.app.problem.domain.search.filter;

import com.querydsl.core.*;
import org.app.problem.domain.exception.*;

public interface FilterClausesBuilderStrategy {

    void addFilterClauses(
            BooleanBuilder filteringClauses, ProblemFilter<?> filter
    ) throws FilterTypeMismatchException, FailedToCastFilterValueException;

    ProblemFilterType handleableFilterType();

}
