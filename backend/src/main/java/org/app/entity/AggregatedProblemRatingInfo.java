package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Embeddable
public class AggregatedProblemRatingInfo {

    private long numOfTotalRatings = 0L;

    private long sumOfTotalRatingScore = 0L;

    public double getRatingAverage() {
        return (double) sumOfTotalRatingScore / numOfTotalRatings;
    }

    public void increaseNumOfTotalRatings() {
        this.numOfTotalRatings++;
    }

    public void addRatingScore(long addition) {
        this.sumOfTotalRatingScore += addition;
    }
}
