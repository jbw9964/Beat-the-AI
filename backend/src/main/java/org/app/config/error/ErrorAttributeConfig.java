package org.app.config.error;

import java.util.*;
import lombok.*;
import org.app.util.*;
import org.springframework.boot.web.error.*;
import org.springframework.boot.web.servlet.error.*;
import org.springframework.stereotype.*;
import org.springframework.web.context.request.*;

@Component
@RequiredArgsConstructor
public class ErrorAttributeConfig extends DefaultErrorAttributes {

    private final MdcIdConfigurer mdcIdConfigurer;

    @Override
    public Map<String, Object> getErrorAttributes(
            WebRequest webRequest,
            ErrorAttributeOptions options
    ) {
        var errorAttributes = super.getErrorAttributes(webRequest, options);

        String requestId = mdcIdConfigurer.currentRequestId();
        errorAttributes.put("requestId", requestId);

        return errorAttributes;
    }
}
