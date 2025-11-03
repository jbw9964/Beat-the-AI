package org.app.util.api;

import jakarta.validation.constraints.*;

public record SimplePageRequest(
        @Min(value = 0, message = "페이지 번호는 0 보다 크거나 같아야 합니다")
        int pageNum,
        @Min(value = 1, message = "페이지 크기는 1 보다 크거나 같아야 합니다")
        int pageSize
) {

    public SimplePageRequest() {
        this(0, 10);
    }
}
