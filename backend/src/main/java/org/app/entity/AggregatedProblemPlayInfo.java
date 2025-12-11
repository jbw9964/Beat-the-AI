package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Embeddable
public class AggregatedProblemPlayInfo {

    private long numOfTotalPlays = 0L;

    public void increaseNumOfTotalPlays() {
        this.numOfTotalPlays++;
    }

}
