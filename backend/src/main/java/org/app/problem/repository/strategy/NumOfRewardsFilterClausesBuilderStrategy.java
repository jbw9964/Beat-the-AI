package org.app.problem.repository.strategy;

import com.querydsl.core.*;
import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class NumOfRewardsFilterClausesBuilderStrategy
        extends AbstractFilterClausesBuilder<Integer> {

    protected NumOfRewardsFilterClausesBuilderStrategy() {
        super(Integer.class);
    }

    @Override
    public void addFilterClauses(BooleanBuilder filteringClauses, ProblemFilter<?> filter)
            throws FilterTypeMismatchException, FailedToCastFilterValueException {

        super.throwExOnFilterTypeMismatch(filter.getFilterType());

        Integer from = super.castValueOrThrowEx(filter.getFrom());
        Integer to = super.castValueOrThrowEx(filter.getTo());

        BooleanExpression betweenValues
                = super.QPA_TARGET.problemInfo.numOfRewardSet.between(from, to);

        super.addAndPredicate(filteringClauses, betweenValues);
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.NUM_OF_REWARDS;
    }
}
