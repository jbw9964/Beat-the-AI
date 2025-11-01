package org.app.auth.dto.request;

import jakarta.validation.constraints.*;

public record AuthSignupRequest(
        @NotBlank(message = "로그인 ID 를 제공해주세요")
        String loginId,
        @NotBlank(message = "PW 를 제공해주세요")
        String password,
        @NotBlank(message = "이름을 제공해주세요")
        String name,
        @Email(message = "이메일 형식이 잘못되었습니다")
        String email
) {

}
