package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Embeddable
public class AggregatedProblemPlayInfo {

    @Column(nullable = false)
    private long numOfTotalPlays;

    public AggregatedProblemPlayInfo() {
        this(0);
    }

    public AggregatedProblemPlayInfo(long numOfTotalPlays) {
        this.numOfTotalPlays = numOfTotalPlays;
    }

    public void increaseNumOfTotalPlays() {
        this.numOfTotalPlays++;
    }

}
