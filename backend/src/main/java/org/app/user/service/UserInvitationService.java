package org.app.user.service;

import java.time.*;
import java.util.*;
import java.util.stream.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.user.repository.*;
import org.app.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springframework.dao.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserInvitationService {

    private final GlobalUtil globalUtil;
    private final UserRepository userRepo;

    private final UserInvitationRepository invitationRepo;
    private final UserReceivedInvitationRepository receivedInvitationRepo;


    // 초대받은 목록 보기. 이전에 수령한 초대가 유효하지 않으면 응답 속성으로 보여줌.
    public SimplePageResponse<ReceivedInvitationInfo> getMyReceivedInvitations(
            Long userId, int pageNo, int pageSize
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        Pageable pageable = globalUtil.pageable(pageNo, pageSize);
        Page<ReceivedInvitation> find = receivedInvitationRepo.findByUserId(userId, pageable);

        // 주어진 코드가 유효한지 확인한다
        List<String> invitedCodeList = find.map(ReceivedInvitation::getCode).toList();

        // 살아있는 코드만 뽑아낸다
        Set<String> activeCodeSet = invitationRepo.findAllByCodes(invitedCodeList).stream()
                .map(Invitation::getCode).collect(Collectors.toSet());

        return globalUtil.toSimplePageResponse(find, entity -> {
            String code = entity.getCode();
            boolean isActive = activeCodeSet.contains(code);
            return Util.toInfo(entity, isActive);
        });
    }

    // 초대 코드를 수령한다.
    @Transactional
    public Long receiveInvitation(Long userId, String invitationCode) {

        User find = this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        Invitation correspondingInvitation
                = invitationRepo.findByCodeFetchingProblem(invitationCode)
                .orElseThrow(InactiveInvitationCodeException::new);

        Problem problem = correspondingInvitation.getProblem();
        Long problemId = problem.getId();
        String problemTitle = problem.getTitle();
        ReceivedInvitation newEntity = new ReceivedInvitation(
                find, problemId, problemTitle, invitationCode
        );

        try {
            return receivedInvitationRepo.save(newEntity).getId();
        } catch (DataIntegrityViolationException e) {
            log.warn("Failed to save entity: {}", e.getMessage(), e);
            throw new AlreadyReceivedInvitationException();
        }
    }

    // 내가 수령한 초대 코드 내용 보기
    public ReceivedInvitationInfo getMyReceivedInvitation(Long userId, Long receivedInvitationId) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        ReceivedInvitation find = receivedInvitationRepo.findById(receivedInvitationId)
                .orElseThrow(ReceivedInvitationNotFoundException::new);

        if (!find.getUser().getId().equals(userId)) {
            throw new ForbiddenException("자신이 수령한 코드만 조회할 수 있습니다.");
        }

        String code = find.getCode();
        boolean isActive = invitationRepo.existsByCode(code);

        return Util.toInfo(find, isActive);
    }

    // 수령했던 코드 삭제하기
    @Transactional
    public Long deleteMyInvitation(Long userId, Long receivedInvitationId) {
        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        ReceivedInvitation find = receivedInvitationRepo.findById(receivedInvitationId)
                .orElseThrow(ReceivedInvitationNotFoundException::new);

        if (!find.getUser().getId().equals(userId)) {
            throw new ForbiddenException("자신이 수령한 코드만 삭제할 수 있습니다.");
        }

        receivedInvitationRepo.delete(find);

        return receivedInvitationId;
    }

    private User findNonWithdrawnUserOrThrowUserNotFoundEx(Long userId) {
        return globalUtil.getNonSoftDeltedOrThrow(
                userId, userRepo::findById, UserNotFoundException::new
        );
    }

    private record Util() {

        static ReceivedInvitationInfo toInfo(ReceivedInvitation entity, boolean isActive) {
            Long receivedInvitationId = entity.getId();
            Long problemId = entity.getProblemId();
            String problemTitle = entity.getProblemTitle();
            String code = entity.getCode();
            LocalDateTime createdAt = entity.getCreatedAt();

            return new ReceivedInvitationInfo(
                    receivedInvitationId, problemId, problemTitle,
                    code, isActive, createdAt
            );
        }
    }
}
