package org.app.problem.repository.strategy;

import com.querydsl.core.*;
import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class NumOfRatingsFilterClausesBuilderStrategy
        extends AbstractFilterClausesBuilder<Long> {

    protected NumOfRatingsFilterClausesBuilderStrategy() {
        super(Long.class);
    }

    @Override
    public void addFilterClauses(BooleanBuilder filteringClauses, ProblemFilter<?> filter)
            throws FilterTypeMismatchException, FailedToCastFilterValueException {

        super.throwExOnFilterTypeMismatch(filter.getFilterType());

        Long from = super.castValueOrThrowEx(filter.getFrom());
        Long to = super.castValueOrThrowEx(filter.getTo());

        BooleanExpression betweenValues
                = super.QPA_TARGET.ratingInfo.numOfTotalRatings.between(from, to);

        super.addAndPredicate(filteringClauses, betweenValues);
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.NUM_OF_RATINGS;
    }
}
