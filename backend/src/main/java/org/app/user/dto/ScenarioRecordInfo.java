package org.app.user.dto;

import java.time.*;

public record ScenarioRecordInfo(
        Long scenarioRecordId,
        int scenarioOrder,
        String scenarioContent,
        String userSubmissionContent,
        String aiGeneratedContent,
        boolean hasSubmitted,
        boolean hasPassed,
        LocalDateTime submittedAt
) {

}
