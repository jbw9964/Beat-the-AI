package org.app.util.api;

import lombok.*;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public final class ApiResponse<T> {

    private final boolean success;
    private final int code;
    private final T data;
    private final String message;

    @Setter(AccessLevel.PUBLIC)
    private String requestId;

    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, null);
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return success(200, data, message);
    }

    public static <T> ApiResponse<T> success(int code, T data, String message) {
        return new ApiResponse<>(true, code, data, message);
    }

    public static <T> ApiResponse<T> fail(int code, T data, String message) {
        return new ApiResponse<>(false, code, data, message);
    }
}
