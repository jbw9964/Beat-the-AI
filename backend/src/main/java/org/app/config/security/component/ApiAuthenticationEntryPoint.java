package org.app.config.security.component;

import com.fasterxml.jackson.databind.*;
import jakarta.servlet.http.*;
import java.io.*;
import java.nio.charset.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.util.*;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.security.web.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final int CODE = HttpStatus.UNAUTHORIZED.value();

    private final ObjectMapper objMapper;
    private final MdcIdConfigurer mdcIdConfigurer;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        log.info("ApiAuthenticationEntryPoint has been invoked");

        String message = String.format(
                "API 요청이 거절되었습니다 : %s", authException.getMessage()
        );

        String requestId = mdcIdConfigurer.currentRequestId();
        var respBody = new ApiUnauthenticatedResponseBody(
                false, CODE, message, requestId
        );

        response.setStatus(respBody.code());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objMapper.writeValueAsString(respBody));
    }
}

record ApiUnauthenticatedResponseBody(
        boolean success,
        int code,
        String message,
        String requestId
) {

}

