package org.app.auth.dto.request;

import jakarta.validation.constraints.*;

public record AuthReissueRequest(
        @NotBlank(message = "재발급 토큰을 제공해주세요")
        String refreshToken
) {

}
