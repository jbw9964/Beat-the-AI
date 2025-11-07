package org.app.user.controller;

import jakarta.validation.*;
import lombok.*;
import org.app.user.dto.*;
import org.app.user.dto.request.*;
import org.app.user.service.*;
import org.app.util.api.*;
import org.springdoc.core.annotations.*;
import org.springframework.security.core.annotation.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/me/received-invitation")
public class UserInvitationController {

    private final UserInvitationService userInvitationService;

    @GetMapping     // 내가 수령한 초대 코드 목록 보기
    public ApiResponse<SimplePageResponse<ReceivedInvitationInfo>> getMyReceivedInvitations(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @ParameterObject @ModelAttribute
            SimplePageRequest pageRequest
    ) {
        int pageNo = pageRequest.pageNum();
        int pageSize = pageRequest.pageSize();

        SimplePageResponse<ReceivedInvitationInfo> response
                = userInvitationService.getMyReceivedInvitations(authedUserId, pageNo, pageSize);

        return ApiResponse.success(response);
    }

    @PostMapping    // 새 초대 코드 수령받기
    public ApiResponse<Long> receiveInvitation(
            @AuthenticationPrincipal Long authedUserId,
            @Valid @RequestBody ReceiveInvitationRequest request
    ) {
        String code = request.invitationCode();

        Long response = userInvitationService.receiveInvitation(authedUserId, code);

        return ApiResponse.created(response);
    }

    // 내가 수령한 초대 코드 내용 보기
    @GetMapping("/{received-invitation-id:\\d+}")
    public ApiResponse<ReceivedInvitationInfo> getMyReceivedInvitation(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("received-invitation-id") Long receivedInvitationId
    ) {
        ReceivedInvitationInfo response = userInvitationService.getMyReceivedInvitation(
                authedUserId, receivedInvitationId
        );

        return ApiResponse.success(response);
    }

    // 받은 수령 코드 삭제하기
    @DeleteMapping("/{received-invitation-id:\\d+}")
    public ApiResponse<Long> deleteMyInvitation(
            @AuthenticationPrincipal Long authedUserId,
            @PathVariable("received-invitation-id") Long receivedInvitationId
    ) {
        Long response = userInvitationService.deleteMyInvitation(authedUserId,
                receivedInvitationId);

        return ApiResponse.success(response);
    }

}
