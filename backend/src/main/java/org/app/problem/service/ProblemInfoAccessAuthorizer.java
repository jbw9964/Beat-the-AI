package org.app.problem.service;

import java.util.*;
import lombok.*;
import org.app.entity.*;
import org.app.problem.repository.*;
import org.springframework.stereotype.*;

@Component
@RequiredArgsConstructor
public class ProblemInfoAccessAuthorizer {

    private final ProblemUserRepository userRepo;

    private final ProblemReceivedInvitationRepository receivedInvitationRepo;
    private final ProblemInvitationRepository invitationRepo;

    public boolean accessable(Problem problem, Long userId) {

        Long createdUserId = problem.getUser().getId();
        ProblemVisibility visibility = problem.getVisibility();

        if (    // 내가 생성한 문제이거나 public 문제publicProblem
                createdUserId.equals(userId) ||
                visibility.equals(ProblemVisibility.PUBLIC)
        ) {
            return true;
        }

        // 내가 만들지 않은 private 문제들

        // 탈퇴한 user?
        if (!this.doesUserValid(userId)) {
            return false;
        }

        Long problemId = problem.getId();

        // user 가 현 문제에 관해 수령한 초대 코드들
        List<String> userReceivedInvitationCodes
                = receivedInvitationRepo.findAllByUserIdAndProblemId(
                        userId, problemId
                ).stream()
                .map(ReceivedInvitation::getCode)
                .toList();

        if (!userReceivedInvitationCodes.isEmpty()) {

            // 현재 active 된 코드 cnt
            long validCodeCnt = invitationRepo.countAllByCodes(
                    userReceivedInvitationCodes
            );

            return validCodeCnt > 0;
        }

        return false;
    }

    private boolean doesUserValid(Long userId) {

        if (userId != null) {
            Optional<User> opt = userRepo.findById(userId);

            if (opt.isPresent()) {
                User find = opt.get();
                return !find.doesRemovalScheduled();
            }
        }

        return false;
    }
}
