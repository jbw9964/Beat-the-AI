package org.app.problem.service.strategy.order;

import com.querydsl.core.types.dsl.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.request.*;
import org.springframework.stereotype.*;

@Component
public class RatingAvgAscOrderStrategy
        extends AbstractOrderRequestAdaptingStrategy {

    public RatingAvgAscOrderStrategy() {
        super(true, NullValueOrderHandling.NULL_LAST);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public ProblemOrder toOrder(OrderingRequest request) throws OrderTypeMismatchException {
        super.throwExOnOrderTypeMismatch(request.orderType());

        // ?? 만들다 보니 embedded ratinginfo 에 ratingAverage 라는 path 가 존재하네...?
        // 뭐지 이건?? 뭔가 메서드 읽어서 auto gen 된것 같은데
        // 관련된 문서 찾기 어려워서 그냥 일반적? 으로 만듬;;
        // super.PROBLEM_AGGREGATION.ratingInfo.ratingAverage;

        NumberPath<Long> numOfTotalRatings
                = super.PROBLEM_AGGREGATION.ratingInfo.numOfTotalRatings;
        NumberPath<Long> sumOfTotalRatingScore
                = super.PROBLEM_AGGREGATION.ratingInfo.sumOfTotalRatingScore;

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
        return ProblemOrderType.RATING_AVG_ASC;
    }

}
