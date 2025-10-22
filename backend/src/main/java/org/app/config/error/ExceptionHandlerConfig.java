package org.app.config.error;

import jakarta.validation.*;
import java.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springframework.validation.*;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class ExceptionHandlerConfig {

    @ExceptionHandler(CustomException.class)
    public ApiResponse<?> handle(CustomException e) {
        int code = e.getCode();
        String message = e.getMessage();
        return ApiResponse.fail(code, null, message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<?> reqArgNotValidException(MethodArgumentNotValidException e) {

        List<String> errorMessages = e.getBindingResult().getFieldErrors().stream()
                .map(Util::fieldErrorMessage)
                .toList();

        return ApiResponse.fail(400, errorMessages, "Bad Request");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<?> constraintViolationException(ConstraintViolationException e) {

        List<String> errorMessages = e.getConstraintViolations()
                .stream()
                .map(Util::constraintViolationMessage)
                .toList();

        return ApiResponse.fail(400, errorMessages, "Bad Request");
    }

    private record Util() {

        static String fieldErrorMessage(FieldError fieldError) {
            return String.format("%s: %s",
                    fieldError.getField(), fieldError.getDefaultMessage()
            );
        }

        static String constraintViolationMessage(ConstraintViolation<?> constraintViolation) {
            return String.format("%s: %s",
                    constraintViolation.getPropertyPath(),
                    constraintViolation.getMessage()
            );
        }
    }
}
