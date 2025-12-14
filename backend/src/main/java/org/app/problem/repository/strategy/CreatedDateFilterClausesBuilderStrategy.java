package org.app.problem.repository.strategy;

import com.querydsl.core.*;
import com.querydsl.core.types.dsl.*;
import java.time.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class CreatedDateFilterClausesBuilderStrategy
        extends AbstractFilterClausesBuilder<LocalDate> {

    protected CreatedDateFilterClausesBuilderStrategy() {
        super(LocalDate.class);
    }

    @Override
    public void addFilterClauses(BooleanBuilder filteringClauses, ProblemFilter<?> filter)
            throws FilterTypeMismatchException, FailedToCastFilterValueException {

        super.throwExOnFilterTypeMismatch(filter.getFilterType());

        LocalDate fromLD = super.castValueOrThrowEx(filter.getFrom());
        LocalDate toLD = super.castValueOrThrowEx(filter.getTo());

        LocalDateTime from = super.getNullOrMappedValue(
                fromLD,
                ld -> ld.atTime(LocalTime.MIN)
        );
        LocalDateTime to = super.getNullOrMappedValue(
                toLD,
                ld -> ld.atTime(LocalTime.MAX)
        );

        BooleanExpression betweenDates = super.QP.createdAt.between(from, to);

        super.addAndPredicate(filteringClauses, betweenDates);
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.CREATED_DATE;
    }

}
