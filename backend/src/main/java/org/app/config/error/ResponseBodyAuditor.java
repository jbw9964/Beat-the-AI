package org.app.config.error;

import lombok.*;
import lombok.extern.slf4j.*;
import org.app.util.*;
import org.app.util.api.*;
import org.springframework.core.*;
import org.springframework.http.*;
import org.springframework.http.converter.*;
import org.springframework.http.server.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.*;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
@SuppressWarnings("NullableProblems")
public class ResponseBodyAuditor implements ResponseBodyAdvice<Object> {

    private final MdcIdConfigurer mdcIdConfigurer;

    @Override
    public boolean supports(
            MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(
            Object body, MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response
    ) {

        if (body != null) {
            if (body instanceof ApiResponse<?> api) {
                var statusCode = HttpStatus.valueOf(api.getCode());
                response.setStatusCode(statusCode);

                api.setRequestId(mdcIdConfigurer.currentRequestId());
            } else {
                log.warn(
                        "Incompatible body type encountered: {}",
                        body.getClass().getSimpleName()
                );
            }
        }

        return body;
    }
}
