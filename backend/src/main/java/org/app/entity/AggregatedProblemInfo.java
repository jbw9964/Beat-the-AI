package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Embeddable
public class AggregatedProblemInfo {

    @Column(nullable = false)
    private int numOfRewardSet;

    @Column(nullable = false)
    private int numOfScenarioSet;

    public AggregatedProblemInfo() {
        this(0, 0);
    }

    public AggregatedProblemInfo(int numOfRewardSet, int numOfScenarioSet) {
        this.numOfRewardSet = numOfRewardSet;
        this.numOfScenarioSet = numOfScenarioSet;
    }

    public void increaseNumOfRewardSet() {
        this.numOfRewardSet++;
    }

    public void decreaseNumOfRewardSet() {
        this.numOfRewardSet--;
    }

    public void changeNumOfScenarioSetTo(int change) {
        this.numOfScenarioSet = change;
    }
}
