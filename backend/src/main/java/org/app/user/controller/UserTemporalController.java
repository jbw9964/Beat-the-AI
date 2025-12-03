package org.app.user.controller;

import jakarta.validation.*;
import lombok.*;
import org.app.user.dto.*;
import org.app.user.dto.request.*;
import org.app.user.service.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springdoc.core.annotations.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/me/temporal")
public class UserTemporalController {

    private final UserTemporalProblemService temporalProblemService;
    private final TemporalProblemInfoAdaptor infoAdaptor;

    // 임시저장 목록보기
    @GetMapping
    public ApiResponse<SimplePageResponse<SimpleTemporalProblemInfo>> getMyTemporalProblems(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        int pageNo = pageRequest.pageNum();
        int pageSize = pageRequest.pageSize();

        SimplePageResponse<SimpleTemporalProblemInfo> response
                = temporalProblemService.getMyTemporalProblems(
                authedUserId, pageNo, pageSize
        );

        return ApiResponse.success(response);
    }

    // 임시저장 내용 보기
    @GetMapping("/{temporal-id:\\d+}")
    public ApiResponse<DetailedTemporalProblemInfo> getMyTemporalProblem(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("temporal-id") Long temporalId
    ) {
        DetailedTemporalProblemInfo response
                = temporalProblemService.getMyTemporalProblem(
                authedUserId, temporalId
        );

        return ApiResponse.success(response);
    }

    // 임시저장 생성하기
    @PostMapping
    public ApiResponse<Long> createTemporalProblem(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @RequestBody CreateTemporalProblemRequest req
    ) {
        if (!infoAdaptor.areScenarioInfosSerializable(req.scenarioInfos())) {
            throw new BadRequestException(
                    "제공한 시나리오 정보는 직렬화 할 수 없습니다. 정보간 순서, null 체크 여부를 확인해 주세요."
            );
        }

        SerializedTemporalProblemInfo info = infoAdaptor.getSerializedInfo(req);

        Long response = temporalProblemService.createTemporalProblem(authedUserId, info);

        return ApiResponse.created(response);
    }

    // 임시저장 수정하기
    @PutMapping("/{temporal-id:\\d+}")
    public ApiResponse<Long> updateTemporalProblem(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("temporal-id") Long temporalId,
            @Valid @RequestBody CreateTemporalProblemRequest req
    ) {
        if (!infoAdaptor.areScenarioInfosSerializable(req.scenarioInfos())) {
            throw new BadRequestException(
                    "제공한 시나리오 정보는 직렬화 할 수 없습니다. 정보간 순서, null 체크 여부를 확인해 주세요."
            );
        }

        SerializedTemporalProblemInfo info = infoAdaptor.getSerializedInfo(req);

        Long response = temporalProblemService.updateTemporalProblem(
                authedUserId, temporalId, info
        );

        return ApiResponse.success(response);
    }

    // 임시저장 삭제하기
    @DeleteMapping("/{temporal-id:\\d+}")
    public ApiResponse<Long> deleteTemporalProblem(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("temporal-id") Long temporalId
    ) {
        Long response = temporalProblemService.deleteTemporalProblem(
                authedUserId, temporalId
        );

        return ApiResponse.success(response);
    }

}
