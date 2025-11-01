package org.app.auth.dto.request;

import jakarta.validation.constraints.*;

public record AuthLoginRequest(
        @NotBlank(message = "ID 를 입력해주세요")
        String loginId,
        @NotBlank(message = "PW 를 입력해주세요")
        String password
) {

}
