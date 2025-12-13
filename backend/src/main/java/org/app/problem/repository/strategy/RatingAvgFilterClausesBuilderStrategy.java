package org.app.problem.repository.strategy;

import com.querydsl.core.*;
import com.querydsl.core.types.dsl.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.springframework.stereotype.*;

@Component
public class RatingAvgFilterClausesBuilderStrategy
        extends AbstractFilterClausesBuilder<Double> {

    protected RatingAvgFilterClausesBuilderStrategy() {
        super(Double.class);
    }

    @Override
    @SuppressWarnings("DuplicatedCode")
    public void addFilterClauses(BooleanBuilder filteringClauses, ProblemFilter<?> filter)
            throws FilterTypeMismatchException, FailedToCastFilterValueException {

        super.throwExOnFilterTypeMismatch(filter.getFilterType());

        Double from = super.castValueOrThrowEx(filter.getFrom());
        Double to = super.castValueOrThrowEx(filter.getTo());

        QAggregatedProblemRatingInfo ratingInfo = super.QPA_TARGET.ratingInfo;
        NumberPath<Long> numOfTotalRatings = ratingInfo.numOfTotalRatings;
        NumberPath<Long> sumOfTotalRatingScore = ratingInfo.sumOfTotalRatingScore;

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

        BooleanExpression betweenValues = getAvgExpression.between(from, to);

        super.addAndPredicate(filteringClauses, betweenValues);
    }

    @Override
    public ProblemFilterType handleableFilterType() {
        return ProblemFilterType.RATING_AVG;
    }
}
