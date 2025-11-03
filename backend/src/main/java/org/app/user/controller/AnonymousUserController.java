package org.app.user.controller;

import jakarta.validation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.user.dto.response.*;
import org.app.user.service.*;
import org.app.util.api.*;
import org.springdoc.core.annotations.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/user/{user_id:\\d+}")
@RequiredArgsConstructor
public class AnonymousUserController {

    private final AnonymousUserService anonymousUserService;

    // 사용자 정보 보기
    @GetMapping
    public ApiResponse<GetUserResponse> getUser(
            @PathVariable("user_id") Long userId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {
        GetUserResponse response = anonymousUserService.getUser(userId, authenticatedUserId);

        return ApiResponse.success(response);
    }

    // public 한 사용자 플레이 기록 (목록) 보기
    @GetMapping("/public-record")
    public ApiResponse<GetPublicRecordsResponse> getPublicRecords(
            @PathVariable("user_id") Long userId,
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {
        // pageRequest :
        // @ModelAttribute 로 SimplePageRequest 속성 이름 받도록 구성
        // @ParameterObject 로 swagger 에서 query param 으로 인식하도록 구성

        int pageNo = pageRequest.getPageNumOrDefault();
        int pageSize = pageRequest.getPageSizeOrDefault();

        GetPublicRecordsResponse response = anonymousUserService.getPublicRecords(
                userId, pageNo, pageSize, authenticatedUserId
        );

        return ApiResponse.success(response);
    }

    // public 한 사용자 플레이 기록 (단일) 보기
    @GetMapping("/public-record/{record_id:\\d+}")
    public ApiResponse<GetPublicRecordResponse> getPublicRecord(
            @PathVariable("user_id") Long userId,
            @PathVariable("record_id") Long recordId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {
        GetPublicRecordResponse response = anonymousUserService.getPublicRecord(
                userId, recordId, authenticatedUserId
        );

        return ApiResponse.success(response);
    }
}
