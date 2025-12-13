package org.app.problem.repository.strategy;

import com.querydsl.core.*;
import com.querydsl.core.types.Predicate;
import java.util.function.*;
import lombok.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.*;
import org.app.problem.domain.search.filter.*;

public abstract class AbstractFilterClausesBuilder<T>
        implements FilterClausesBuilderStrategy {

    protected final QProblem QP = QProblemPaths.QPROBLEM_ROOT;
    protected final QProblemAggregation QPA_TARGET
            = QProblemPaths.QP__QPROBLEM_AGGREGATION_TARGET;

    private final Class<T> expectedValueClassType;

    protected AbstractFilterClausesBuilder(
            @NonNull Class<T> expectedValueClassType
    ) {
        this.expectedValueClassType = expectedValueClassType;
    }

    protected final void throwExOnFilterTypeMismatch(ProblemFilterType given) {
        ProblemFilterType handleableFilterType = this.handleableFilterType();

        if (!given.equals(handleableFilterType)) {
            throw new FilterTypeMismatchException(String.format(
                    "Expected to get [%s]-typed filtering request, "
                    + "but encountered [%s] type on adaptor: %s",
                    handleableFilterType, given, this.getClass().getSimpleName()
            ));
        }
    }

    protected final T castValueOrThrowEx(Object value) {
        try {
            return expectedValueClassType.cast(value);
        } catch (ClassCastException e) {
            throw new FailedToCastFilterValueException(
                    String.format(
                            "Expected to given value(%s) to be null or assigneable from (%s), "
                            + "but (%s) type given.",
                            value, expectedValueClassType.getSimpleName(),
                            value.getClass().getSimpleName()
                    ), e
            );
        }
    }

    protected final <U, R> R getNullOrMappedValue(U value, Function<U, R> mapper) {
        return value == null ? null : mapper.apply(value);
    }

    protected final void addAndPredicate(
            BooleanBuilder booleanBuilder, Predicate predicate
    ) {
        booleanBuilder.and(predicate);
    }
}
