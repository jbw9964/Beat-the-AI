package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Embeddable
public class AggregatedProblemRatingInfo {

    @Column(nullable = false)
    private long numOfTotalRatings;

    @Column(nullable = false)
    private long sumOfTotalRatingScore;

    public AggregatedProblemRatingInfo() {
        this(0L, 0L);
    }

    public AggregatedProblemRatingInfo(
            long numOfTotalRatings, long sumOfTotalRatingScore
    ) {
        this.numOfTotalRatings = numOfTotalRatings;
        this.sumOfTotalRatingScore = sumOfTotalRatingScore;
    }

    public double getRatingAverage() {
        return numOfTotalRatings <= 0 ? 0.d :
                (double) sumOfTotalRatingScore / numOfTotalRatings;
    }

    public void increaseNumOfTotalRatings() {
        this.numOfTotalRatings++;
    }

    public void decreaseNumOfTotalRatings() {
        this.numOfTotalRatings--;
    }

    public void increaseRatingScore(long addition) {
        this.sumOfTotalRatingScore += addition;
    }

    public void decreaseRatingScore(long subtraction) {
        this.sumOfTotalRatingScore -= subtraction;
    }
}
