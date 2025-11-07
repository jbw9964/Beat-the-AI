package org.app.user.dto.request;

import jakarta.validation.constraints.*;

public record ReceiveInvitationRequest(
        @NotEmpty(message = "초대코드를 제공해 주세요.")
        String invitationCode
) {

}
