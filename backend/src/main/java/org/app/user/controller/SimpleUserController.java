package org.app.user.controller;

import jakarta.validation.*;
import lombok.*;
import org.app.user.dto.*;
import org.app.user.dto.request.*;
import org.app.user.dto.response.*;
import org.app.user.service.*;
import org.app.util.api.*;
import org.springdoc.core.annotations.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/me")
@RequiredArgsConstructor
public class SimpleUserController {

    private final SimpleUserService simpleUserService;

    @GetMapping     // 자기 정보 보기
    public ApiResponse<GetUserResponse> getMe(@AuthenticationPrincipal Long authedUserId) {
        GetUserResponse response = simpleUserService.getMe(authedUserId);

        return ApiResponse.success(response);
    }

    @DeleteMapping  // 회원 탈퇴하기
    public ApiResponse<Long> withdrawUser(@AuthenticationPrincipal Long authedUserId) {
        Long response = simpleUserService.withdrawUser(authedUserId);

        return ApiResponse.accepted(response);
    }

    @PutMapping("/info")        // 자기 정보 수정하기
    public ApiResponse<Long> updateInfo(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @RequestBody UpdateInfoRequest request
    ) {
        String newName = request.username();
        String newEmail = request.email();
        String newThumbnail = request.thumbnailUrl();

        Long response = simpleUserService.updateInfo(
                authedUserId, newName, newEmail, newThumbnail
        );

        return ApiResponse.success(response);
    }

    @PutMapping("/setting")     // 자기 설정 수정하기
    public ApiResponse<Long> updateSetting(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @RequestBody UpdateSettingRequest request
    ) {
        Long response = simpleUserService.updateSetting(authedUserId);

        return ApiResponse.success(response);
    }

    @PostMapping("/password")       // 비밀번호 바꾸기
    public ApiResponse<Long> updatePassword(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        String oldPw = request.oldPassword();
        String newPw = request.newPassword();

        Long response = simpleUserService.updatePassword(authedUserId, oldPw, newPw);

        return ApiResponse.success(response);
    }

    @GetMapping("/problem")         // 내가 만든 문제 목록 보기
    public ApiResponse<SimplePageResponse<SimpleProblemInfo>> getMyProblems(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        int pageNo = pageRequest.getPageNumOrDefault();
        int pageSize = pageRequest.getPageSizeOrDefault();

        SimplePageResponse<SimpleProblemInfo> response
                = simpleUserService.getMyProblems(authedUserId, pageNo, pageSize);

        return ApiResponse.success(response);
    }

    // 내가 만든 문제 내용 보기
    @GetMapping("/problem/{problem-id:\\d+}")
    public ApiResponse<DetailedProblemInfo> getMyProblem(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("problem-id") Long problemId
    ) {
        DetailedProblemInfo response = simpleUserService.getMyProblem(authedUserId, problemId);

        return ApiResponse.success(response);
    }

    @GetMapping("/rating")          // 내가 평가한 내용 목록 보기
    public ApiResponse<SimplePageResponse<RatingInfo>> getMyRatings(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        int pageNo = pageRequest.getPageNumOrDefault();
        int pageSize = pageRequest.getPageSizeOrDefault();

        SimplePageResponse<RatingInfo> response
                = simpleUserService.getMyRatings(authedUserId, pageNo, pageSize);

        return ApiResponse.success(response);
    }

    // 내가 평가한 내용 보기
    @GetMapping("/rating/{rating-id:\\d+}")
    public ApiResponse<RatingInfo> getMyRating(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("rating-id") Long ratingId
    ) {
        RatingInfo response = simpleUserService.getMyRating(authedUserId, ratingId);

        return ApiResponse.success(response);
    }

}
