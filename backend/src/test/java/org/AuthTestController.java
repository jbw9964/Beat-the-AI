package org;

import lombok.extern.slf4j.*;
import org.app.config.security.domain.*;
import org.app.util.api.*;
import org.springframework.security.access.prepost.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(AuthTestController.BASE_URL)
public class AuthTestController {

    public static final String BASE_URL = "/api/auth-testing";

    @GetMapping("/public")
    public ApiResponse<String> onPublic() {
        return ApiResponse.success("I'm public!");
    }

    @GetMapping("/anonymous")
    public ApiResponse<String> onAnonymous() {
        return ApiResponse.success("I'm anonymous!");
    }

    @GetMapping("/user")
    public ApiResponse<Long> onUser(
            @AuthenticationPrincipal Long authenticatedUserId
    ) {
        return ApiResponse.success(authenticatedUserId);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("{#authentication != null} && {#authentication.userId() == #userId}")
    public ApiResponse<Long> onPrivate(
            @PathVariable Long userId,
            SimpleUserAuthentication authentication
    ) {
        log.info("Authentication : {}", authentication);
        return ApiResponse.success(userId);
    }
}
