package org.app.problem.service.strategy.order;

import com.querydsl.core.types.*;
import com.querydsl.core.types.OrderSpecifier.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.*;
import org.app.problem.domain.search.order.*;

public abstract class AbstractOrderRequestAdaptingStrategy
        implements OrderRequestAdaptingStrategy {

    protected final QProblem QP = QProblemExpressions.QPROBLEM_ROOT;
    protected final QProblemAggregation QPA_TARGET
            = QProblemExpressions.QP__QPROBLEM_AGGREGATION_TARGET;

    private final boolean ascendingOrder;
    private final NullValueOrderHandling nullValueOrderHandling;

    protected AbstractOrderRequestAdaptingStrategy(
            boolean ascendingOrder,
            NullValueOrderHandling nullValueOrderHandling
    ) {
        this.ascendingOrder = ascendingOrder;
        this.nullValueOrderHandling = nullValueOrderHandling;
    }

    protected final void throwExOnOrderTypeMismatch(ProblemOrderType given) {
        ProblemOrderType handleableOrderType = this.handleableOrderType();

        if (!given.equals(handleableOrderType)) {
            throw new OrderTypeMismatchException(String.format(
                    "Expected to get [%s]-typed ordering request, "
                    + "but encountered [%s] type on adaptor: %s",
                    handleableOrderType, given, this.getClass().getSimpleName()
            ));
        }
    }

    protected final <T extends Comparable<?>> ProblemOrder buildProblemOrder(
            Expression<T> expression
    ) {
        OrderSpecifier<T> orderSpecifier = this.buildOrderSpecifier(expression);

        return () -> orderSpecifier;
    }

    private <T extends Comparable<?>> OrderSpecifier<T> buildOrderSpecifier(
            Expression<T> expression
    ) {
        Order order = this.ascendingOrder ? Order.ASC : Order.DESC;
        NullHandling nullHandling = this.resolveNullHandling();

        return new OrderSpecifier<>(
                order, expression, nullHandling
        );
    }

    @SuppressWarnings("UnnecessaryDefault")
    private NullHandling resolveNullHandling() {
        return switch (this.nullValueOrderHandling) {
            case NULL_FIRST -> NullHandling.NullsFirst;
            case NULL_LAST -> NullHandling.NullsLast;
            default -> NullHandling.Default;
        };
    }

    protected enum NullValueOrderHandling {
        NULL_FIRST, NULL_LAST
    }
}
