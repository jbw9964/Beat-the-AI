package org;

import jakarta.validation.*;
import jakarta.validation.constraints.*;
import java.io.*;
import lombok.extern.slf4j.*;
import org.app.util.api.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

@Slf4j
@RestController
@RequestMapping(MultipartTestController.BASE_URL)
public class MultipartTestController {

    public static final String BASE_URL = "/api/multipart-testing";
    public static final String MULTIPART_FILE_NAME = "file";
    public static final String MULTIPART_BODY_NAME = "body";

    private static void logMultipart(MultipartFile multipartFile) {
        String name = multipartFile.getName();
        String originalFilename = multipartFile.getOriginalFilename();
        String contentType = multipartFile.getContentType();
        long size = multipartFile.getSize();

        log.info("name: {}", name);
        log.info("originalFilename: {}", originalFilename);
        log.info("contentType: {}", contentType);
        log.info("size: {}", size);

        try {
            byte[] bytes = multipartFile.getBytes();
            log.info("bytes.length: {}", bytes.length);
        } catch (IOException e) {
            log.info("Failed to get bytes", e);
        }
    }

    @PostMapping()
    public ApiResponse<Response> withBody(
            @RequestPart(name = MULTIPART_FILE_NAME)
            MultipartFile multipartFile,
            @Valid @RequestPart(name = MULTIPART_BODY_NAME)
            Body body
    ) {
        logMultipart(multipartFile);

        log.info("Body: {}", body);

        return ApiResponse.success(new Response(
                multipartFile, body
        ));
    }

    public record Response(
            String name,
            String originalFilename,
            String contentType,
            long size,
            Body body
    ) {

        public Response(MultipartFile multipartFile, Body body) {
            this(
                    multipartFile.getName(),
                    multipartFile.getOriginalFilename(),
                    multipartFile.getContentType(),
                    multipartFile.getSize(),
                    body
            );
        }
    }

    public record Body(
            @NotBlank(message = ERR_MSG)
            String message
    ) {

        public static final String ERR_MSG = "메시지를 제공해 주세요.";
    }
}
