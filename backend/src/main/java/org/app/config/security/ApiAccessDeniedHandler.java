package org.app.config.security;

import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.util.*;
import org.springframework.http.*;
import org.springframework.security.access.*;
import org.springframework.security.web.access.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
@RequiredArgsConstructor
class ApiAccessDeniedHandler implements AccessDeniedHandler {

    private static final int CODE = 403;

    private final ObjectMapper objMapper;
    private final MdcIdConfigurer mdcIdConfigurer;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        log.info("ApiAccessDeniedHandler has been invoked");

        String message = String.format(
                "API 요청이 거절되었습니다 : %s", accessDeniedException.getMessage()
        );
        String requestId = mdcIdConfigurer.currentRequestId();
        var respBody = new ApiAccessDeniedResponseBody(
                false, CODE, message, requestId
        );

        response.setStatus(respBody.code());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objMapper.writeValueAsString(respBody));
    }
}

record ApiAccessDeniedResponseBody(
        boolean success,
        int code,
        String message,
        String requestId
) {

}
