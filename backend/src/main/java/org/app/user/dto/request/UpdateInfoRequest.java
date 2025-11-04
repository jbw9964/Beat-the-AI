package org.app.user.dto.request;

import jakarta.validation.constraints.*;

public record UpdateInfoRequest(
        @NotBlank(message = "사용자 이름을 제공해주세요.")
        @Size(message = "사용자 이름은 20자 이하여야 합니다.", max = 20)
        String username,

        @Email(message = "이메일 형식이 올바르지 않습니다.")
        @Size(message = "이메일은 50자 이하여야 합니다.", max = 50)
        String email,

        @Size(message = "썸네일 이미지 주소는 255자 이하여야 합니다.", max = 255)
        String thumbnailUrl
) {

}
