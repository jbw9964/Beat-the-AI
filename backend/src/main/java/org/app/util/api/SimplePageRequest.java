package org.app.util.api;

import com.fasterxml.jackson.annotation.*;
import jakarta.validation.constraints.*;

public record SimplePageRequest(
        @Min(value = 0, message = "페이지 번호는 0 보다 크거나 같아야 합니다")
        Integer pageNum,
        @Min(value = 1, message = "페이지 크기는 1 보다 크거나 같아야 합니다")
        Integer pageSize
) {

    private static final int
            DEFAULT_PAGE_NUM = 0,
            DEFAULT_PAGE_SIZE = 10;

    @JsonIgnore
    public int getPageNumOrDefault() {
        return pageNum == null ? DEFAULT_PAGE_NUM : pageNum;
    }

    @JsonIgnore
    public int getPageSizeOrDefault() {
        return pageSize == null ? DEFAULT_PAGE_SIZE : pageSize;
    }

    // 몰랐는데 record 여도 accessor method 를 명시할 수 있다칸다. JLS 명세에 정확한 규칙 나타나 있음.
    // 참고 : https://docs.oracle.com/javase/specs/jls/se21/html/jls-8.html#jls-8.10.3
    @Override
    public Integer pageNum() {
        return getPageNumOrDefault();
    }

    @Override
    public Integer pageSize() {
        return getPageSizeOrDefault();
    }
}
