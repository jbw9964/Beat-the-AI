package org.app.problem.service.strategy.order;

import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.springframework.stereotype.*;

@Component
public class RatingAvgDescOrderStrategy
        extends AbstractOrderRequestAdaptor {

    public RatingAvgDescOrderStrategy() {
        super(false, NullValueOrderHandling.NULL_LAST);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public ProblemOrder toOrder(OrderingRequest request) throws OrderTypeMismatchException {
        super.throwExOnOrderTypeMismatch(request.orderType());

        NumberPath<Long> numOfTotalRatings
                = super.QPA_TARGET.ratingInfo.numOfTotalRatings;
        NumberPath<Long> sumOfTotalRatingScore
                = super.QPA_TARGET.ratingInfo.sumOfTotalRatingScore;

        NumberExpression<Double> getAvgExpression = new CaseBuilder()
                .when(
                        numOfTotalRatings.isNotNull()
                                .and(numOfTotalRatings.gt(0))
                )
                .then(
                        sumOfTotalRatingScore.nullif(0L)
                                .doubleValue()
                                .divide(numOfTotalRatings)
                )
                .otherwise(0.d);

        return super.buildProblemOrder(getAvgExpression);
    }

    @Override
    public ProblemOrderType handleableOrderType() {
        return ProblemOrderType.RATING_AVG_DESC;
    }
}
