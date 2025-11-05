package org.app.user.dto;

import java.time.*;
import org.app.entity.*;

public record SimpleProblemInfo(
        Long problemId,
        String title,
        String description,
        String rewardMessage,
        int numOfScenariosToGetReward,
        int numOfScenariosToFailPlay,
        ProblemVisibility visibility,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

}
