package org.app.user.dto;

import java.time.*;
import org.app.entity.*;

public record SimpleTemporalProblemInfo(
        Long temporalProblemId,
        String title,
        String description,
        String rewardMessage,
        ProblemVisibility visibility,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

}
