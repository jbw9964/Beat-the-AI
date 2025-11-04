package org.app.user.dto.request;

import jakarta.validation.constraints.*;

public record ChangePasswordRequest(
        @NotBlank(message = "이전 비밀번호를 제공해주세요.")
        String oldPassword,

        @NotBlank(message = "바꿀 비밀번호를 제공해주세요.")
        @Size(message = "비밀번호는 4 자리 이상이어야 합니다.", min = 4)
        String newPassword
) {

}
