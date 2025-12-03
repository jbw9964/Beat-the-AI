package org.app.user.dto.request;

import jakarta.validation.constraints.*;
import org.app.entity.*;

public record ChangeRecordVisibilityRequest(
        @NotNull(message = "변경할 공개 속성을 제공해주세요.")
        PlayRecordVisibility visibility
) {

}
