package org.app.user.dto;

import java.time.*;

public record GainedRewardInfo(
        Long gainedRewardId,
        Long playRecordId,
        String description,
        LocalDateTime createdAt
) {

}
