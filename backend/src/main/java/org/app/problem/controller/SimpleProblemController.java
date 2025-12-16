package org.app.problem.controller;

import jakarta.validation.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.entity.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.*;
import org.app.problem.dto.request.*;
import org.app.problem.dto.response.*;
import org.app.problem.service.*;
import org.app.util.api.*;
import org.springdoc.core.annotations.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/problem")
@RequiredArgsConstructor
public class SimpleProblemController {

    private final SimpleProblemService problemService;
    private final FilterRequestAdaptor filterRequestAdaptor;
    private final OrderRequestAdaptor orderRequestAdaptor;
    private final ProblemInfoAdaptor problemInfoAdaptor;

    // 일반 공개 문제 목록 조회하기
    @GetMapping
    public ApiResponse<SimplePageResponse<SimplePublicProblemInfo>> getPublicProblems(
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        int pageNo = pageRequest.pageNum();
        int pageSize = pageRequest.pageSize();

        SimplePageResponse<SimplePublicProblemInfo> response
                = problemService.getPublicProblems(pageNo, pageSize);

        return ApiResponse.success(response);
    }

    // 세부 공개 문제 검색하기
    @PostMapping("/search-public")
    public ApiResponse<SearchProblemResponse> searchPublicProblems(
            @Valid @RequestBody
            ProblemSearchRequest searchRequest
    ) {
        List<ProblemFilter<?>> problemFilters
                = filterRequestAdaptor.convertToProblemFilterQuery(
                searchRequest.filteringRequests()
        );
        List<ProblemOrder> problemOrders
                = orderRequestAdaptor.convertToProblemOrderQuery(
                searchRequest.orderingRequests()
        );
        SimplePageRequest pageRequest = searchRequest.pagingRequest();

        int pageNo = pageRequest.pageNum();
        int pageSize = pageRequest.pageSize();

        SimplePageResponse<SimplePublicProblemInfo> response = problemService.searchPublicProblems(
                problemFilters, problemOrders, pageNo, pageSize
        );

        return ApiResponse.success(SearchProblemResponse.of(
                response, problemFilters, problemOrders
        ));
    }

    // 문제 정보 조회하기
    @GetMapping("/{problem-id:\\d+}")
    public ApiResponse<DetailedProblemInfo> getProblem(
            @PathVariable("problem-id") Long problemId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {
        DetailedProblemInfo response = problemService.getProblem(
                problemId, authenticatedUserId
        );

        return ApiResponse.success(response);
    }

    // 문제 생성하기
    @PostMapping
    public ApiResponse<Long> createProblem(
            @AuthenticationPrincipal Long authenticatedUserId,
            @Valid @RequestBody CreateProblemRequest req
    ) {
        SerializedProblemCreationInfo serializedCreationInfo
                = problemInfoAdaptor.getSerializedInfoOrThrowEx(req);

        Long response = problemService.createProblem(
                authenticatedUserId, serializedCreationInfo
        );

        return ApiResponse.created(response);
    }

    // 문제 수정하기
    @PutMapping("/{problem-id:\\d+}")
    public ApiResponse<Long> updateProblem(
            @PathVariable("problem-id") Long problemId,
            @AuthenticationPrincipal Long authenticatedUserId,
            @Valid @RequestBody UpdateProblemRequest req
    ) {

        SerializedProblemUpdateInfo serializedUpdateInfo
                = problemInfoAdaptor.getSerializedInfoOrThrowEx(req);

        Long response = problemService.updateProblem(
                problemId, authenticatedUserId, serializedUpdateInfo
        );

        return ApiResponse.success(response);
    }

    // 문제 공개 속성 바꾸기
    @PatchMapping("/{problem-id:\\d+}/visibility")
    public ApiResponse<Long> updateProblemVisibility(
            @PathVariable("problem-id") Long problemId,
            @AuthenticationPrincipal Long authenticatedUserId,
            @Valid @RequestBody UpdateProblemVisibilityRequest req
    ) {
        ProblemVisibility chageTo = req.visibility();

        Long response = problemService.updateProblemVisibility(
                problemId, authenticatedUserId, chageTo
        );

        return ApiResponse.success(response);
    }

    // 문제 삭제하기
    @DeleteMapping("/{problem-id:\\d+}")
    public ApiResponse<Long> deleteProblem(
            @PathVariable("problem-id") Long problemId,
            @AuthenticationPrincipal Long authenticatedUserId
    ) {

        Long response = problemService.deleteProblem(problemId, authenticatedUserId);

        return ApiResponse.accepted(response);
    }
}
