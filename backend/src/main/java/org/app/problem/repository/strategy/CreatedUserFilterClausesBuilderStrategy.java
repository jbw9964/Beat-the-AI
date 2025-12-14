package org.app.problem.repository.strategy;

import com.querydsl.core.*;
import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class CreatedUserFilterClausesBuilderStrategy
        extends AbstractFilterClausesBuilder<Long> {

    protected CreatedUserFilterClausesBuilderStrategy() {
        super(Long.class);
    }

    @Override
    public void addFilterClauses(BooleanBuilder filteringClauses, ProblemFilter<?> filter)
            throws FilterTypeMismatchException, FailedToCastFilterValueException {

        super.throwExOnFilterTypeMismatch(filter.getFilterType());

        Long userId = super.castValueOrThrowEx(filter.getEqualTo());

        BooleanExpression idEq = super.QP.user.id.eq(userId);

        super.addAndPredicate(filteringClauses, idEq);
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.CREATED_USER;
    }
}
