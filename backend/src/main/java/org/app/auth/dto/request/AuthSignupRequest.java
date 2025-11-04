package org.app.auth.dto.request;

import jakarta.validation.constraints.*;

public record AuthSignupRequest(
        @NotBlank(message = "로그인 ID 를 제공해주세요")
        String loginId,

        @NotBlank(message = "비밀번호를 제공해주세요")
        @Size(message = "비밀번호는 4 자리 이상이어야 합니다.", min = 4)
        String password,

        @NotBlank(message = "이름을 제공해주세요")
        @Size(message = "사용자 이름은 20자 이하여야 합니다.", max = 20)
        String name,

        @Email(message = "이메일 형식이 잘못되었습니다")
        @Size(message = "이메일은 50자 이하여야 합니다.", max = 50)
        String email
) {

}
