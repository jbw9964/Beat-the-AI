package org.app.problem.dto;

import java.time.*;

public record ProblemRewardInfo(
        Long problemRewardId,
        String description,
        boolean hasTransferred,
        LocalDateTime createdAt
) {

}
