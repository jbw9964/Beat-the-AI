package org.app.user.controller;

import jakarta.validation.*;
import lombok.*;
import org.app.entity.*;
import org.app.user.dto.*;
import org.app.user.dto.request.*;
import org.app.user.dto.response.*;
import org.app.user.service.*;
import org.app.util.api.*;
import org.springdoc.core.annotations.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/me/record")
public class UserRecordController {

    private final UserRecordService userRecordService;

    // 플레이 기록 목록 보기
    @GetMapping
    public ApiResponse<SimplePageResponse<SimplePlayRecordInfo>> getMyRecords(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        int pageNo = pageRequest.pageNum();
        int pageSize = pageRequest.pageSize();

        SimplePageResponse<SimplePlayRecordInfo> response = userRecordService.getMyRecords(
                authedUserId, pageNo, pageSize
        );

        return ApiResponse.success(response);
    }

    // 플레이 내용 보기
    @GetMapping("/{play-record-id:\\d+}")
    public ApiResponse<GetMyRecordResponse> getMyRecord(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("play-record-id") Long playRecordId
    ) {
        GetMyRecordResponse response = userRecordService.getMyRecord(authedUserId, playRecordId);

        return ApiResponse.success(response);
    }

    // 플레이 기록 공개 속성 바꾸기
    @PatchMapping("/{play-record-id:\\d+}/visibility")
    public ApiResponse<Long> changeMyRecordVisibility(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("play-record-id") Long playRecordId,
            @Valid @RequestBody ChangeRecordVisibilityRequest request
    ) {
        PlayRecordVisibility visibility = request.visibility();

        Long response = userRecordService.changeMyRecordVisibility(
                authedUserId, playRecordId, visibility
        );

        return ApiResponse.success(response);
    }

    // 플레이 기록과 연관된 보상 목록 보기
    @GetMapping("/{play-record-id:\\d+}/reward")
    public ApiResponse<SimplePageResponse<GainedRewardInfo>> getMyRewards(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("play-record-id") Long playRecordId,
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        int pageNo = pageRequest.pageNum();
        int pageSize = pageRequest.pageSize();

        SimplePageResponse<GainedRewardInfo> response = userRecordService.getMyRewards(
                authedUserId, playRecordId, pageNo, pageSize
        );

        return ApiResponse.success(response);
    }

    // 플레이 기록과 연관된 보상 정보 보기 (실 이미지 전달 X)
    @GetMapping("/{play-record-id:\\d+}/reward/{gained-reward-id:\\d+}")
    public ApiResponse<GainedRewardInfo> getMyReward(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("play-record-id") Long playRecordId,
            @PathVariable("gained-reward-id") Long gainedRewardId
    ) {
        GainedRewardInfo response = userRecordService.getMyReward(
                authedUserId, playRecordId, gainedRewardId
        );

        return ApiResponse.success(response);
    }

    // 플레이 기록과 연관된 모든 보상들 삭제하기
    @DeleteMapping("/{play-record-id:\\d+}/reward")
    public ApiResponse<DeleteMyRewardResponse> deleteMyRewards(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("play-record-id") Long playRecordId
    ) {
        DeleteMyRewardResponse response = userRecordService.deleteMyRewards(
                authedUserId, playRecordId
        );

        return ApiResponse.success(response);
    }
}
