package org.app.user.dto;

import java.time.*;
import org.app.entity.*;

public record SimplePlayRecordInfo(
        Long playRecordId,
        Long problemId,
        String title,
        String description,
        PlayRecordStatus status,
        PlayRecordVisibility visibility,
        LocalDateTime createdAt
) {

}
