package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Embeddable
public class AggregatedProblemInfo {

    private int numOfRewardSet = 0;

    private int numOfScenarioSet = 0;

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
