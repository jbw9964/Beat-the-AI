package org;

import jakarta.validation.*;
import lombok.extern.slf4j.*;
import org.app.util.api.*;
import org.springdoc.core.annotations.*;
import org.springframework.context.annotation.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Profile("test")
@RestController
@RequestMapping(PagingTestController.BASE_URL)
public class PagingTestController {

    protected static final String BASE_URL = "/api/paging-testing";
    private static final String GET = "/get";
    public static final String PAGING_TEST_URL = BASE_URL + GET;

    @GetMapping(GET)
    public ApiResponse<Response> get(
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        log.info("Given page request: {}", pageRequest);

        int pageNum = pageRequest.getPageNumOrDefault();
        int pageSize = pageRequest.getPageSizeOrDefault();
        log.info("pageNum: {}, pageSize: {}", pageNum, pageSize);

        return ApiResponse.success(
                new Response(pageNum, pageSize)
        );
    }

    public record Response(
            int pageNum, int pageSize
    ) {

    }
}
