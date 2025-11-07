package org.app.user.dto;

import java.time.*;

public record ReceivedInvitationInfo(
        Long receivedInvitationId,
        Long problemId,
        String problemTitle,
        String code,
        boolean isActive,
        LocalDateTime createdAt
) {

}
