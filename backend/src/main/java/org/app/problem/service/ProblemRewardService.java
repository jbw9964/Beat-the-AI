package org.app.problem.service;

import java.time.*;
import lombok.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.dto.*;
import org.app.problem.dto.response.*;
import org.app.problem.repository.*;
import org.app.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class ProblemRewardService {

    private final GlobalUtil globalUtil;

    private final ProblemRepository problemRepo;
    private final ProblemRewardRepository problemRewardRepo;

    private final ProblemActualRewardImageRepository actualRewardImageRepo;
    private final ProblemOverviewRewardImageRepository overviewRewardImageRepo;
    private final ProblemUserRepository userRepo;

    private final ProblemInfoAccessAuthorizer accessAuthorizer;

    // 문제 설정된 보상 목록 보기
    public GetProblemRewardsResponse getRewards(
            Long problemId, Long authenticatedUserId,
            int pageNo, int pageSize
    ) {

        Problem problem = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        );

        if (!accessAuthorizer.accessable(problem, authenticatedUserId)) {
            throw new ForbiddenException("해당 문제와 연관된 보상 목록을 조회할 권한이 없습니다.");
        }

        Pageable pageable = globalUtil.pageable(pageNo, pageSize);
        Page<ProblemReward> find = problemRewardRepo.findByProblemId(
                problemId, pageable
        );

        SimplePageResponse<ProblemRewardInfo> pageResponse
                = globalUtil.toSimplePageResponse(find, Utils::toInfo);

        boolean isMine = problem.getUser().getId()
                .equals(authenticatedUserId);

        return new GetProblemRewardsResponse(pageResponse, isMine);
    }

    // 문제 보상 미리보기 이미지 보기
    public Long getOverviewRewardImageId(
            Long problemId, Long problemRewardId, Long authenticatedUserId
    ) {

        Problem problem = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        );

        if (!accessAuthorizer.accessable(problem, authenticatedUserId)) {
            throw new ForbiddenException("해당 보상을 조회할 권한이 없습니다.");
        }

        ProblemReward find = problemRewardRepo.findById(problemRewardId)
                .orElseThrow(ProblemRewardNotFoundException::new);

        return find.getOverviewRewardImage().getId();
    }

    // 실제 문제 보상 이미지 보기
    public Long getActualRewardImageId(
            Long problemId, Long problemRewardId, Long authenticatedUserId
    ) {

        Problem problem = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        );

        Long createdUserId = problem.getUser().getId();
        if (!createdUserId.equals(authenticatedUserId)) {
            throw new ForbiddenException("실제 보상 이미지는 문제 작성자만 접근할 수 있습니다.");
        }

        ProblemReward find = problemRewardRepo.findById(problemRewardId)
                .orElseThrow(ProblemRewardNotFoundException::new);

        return find.getActualRewardImage().getId();
    }

    // 문제 보상 추가하기 전 문제 존재하는지, 사용자 탈퇴 안하고 문제 작성자 맞는지 검사
    public void validateBeforeCreateReward(
            Long problemId, Long authenticatedUserId
    ) {

        Problem problem = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        );

        User user = this.findNonWithdrawnUserOrThrowUserNotFoundEx(
                authenticatedUserId
        );

        Long createdUserId = problem.getUser().getId();
        if (!createdUserId.equals(user.getId())) {
            throw new ForbiddenException("문제 작성자만 보상을 추가할 수 있습니다.");
        }
    }

    // 문제 보상 추가하기
    @Transactional
    public Long createReward(
            Long problemId, Long actualRewardImageId,
            Long overviewRewardImageId, String rewardDescription
    ) {
        Problem problem = globalUtil.getNonSoftDeltedOrThrow(
                problemId, problemRepo::findById,
                () -> new UnexpectedNotFoundException(
                        "Expected to problem exists, but found nothing."
                )
        );

        // TODO : 지금 jpql 보니까 모든 sub table 들을 left join 함.
        //  다른 타입일 때는 별 상관 없지만 DB storage 일 때는 image 정보까지 가져오기 때문에 문제가 될 듯.
        //  jpa + 다형성 특성상 어쩔수 없는건 이해함. 그래도 뭔가 조치를 취하긴 해야될 것 같음.
        //  추가로 Long, Integer 만으로 fk 제약조건 걸수있나 해봤는데... 일단 잘 안됨.
        //  여튼 뭔가로든 해결 해야됨. 차라리 테이블 나누든가... 아님 뭐 어찌 하든가 몰루
        ActualRewardImage actualRewardImage = actualRewardImageRepo.findById(
                        actualRewardImageId
                )
                .orElseThrow(() -> new UnexpectedNotFoundException(
                        "Expected to actual reward image exists, but found nothing."
                ));
        OverviewRewardImage overviewRewardImage = overviewRewardImageRepo.findById(
                        overviewRewardImageId
                )
                .orElseThrow(() -> new UnexpectedNotFoundException(
                        "Expected to overview reward image exists, but found nothing."
                ));

        ProblemReward newEntity = new ProblemReward(
                problem, rewardDescription, actualRewardImage, overviewRewardImage
        );

        problem.increaseNumOfRewardSets();

        return problemRewardRepo.save(newEntity).getId();
    }

    // 문제 보상 설명 수정하기
    @Transactional
    public Long updateRewardDescription(
            Long problemId, Long problemRewardId, Long authenticatedUserId, String description
    ) {

        Problem problem = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        );

        User user = this.findNonWithdrawnUserOrThrowUserNotFoundEx(
                authenticatedUserId
        );

        Long createdUserId = problem.getUser().getId();
        if (!createdUserId.equals(user.getId())) {
            throw new ForbiddenException("문제 작성자만 보상 설명을 수정할 수 있습니다.");
        }

        ProblemReward find = problemRewardRepo.findById(problemRewardId)
                .orElseThrow(ProblemRewardNotFoundException::new);

        find.changeDescription(description);

        return find.getId();
    }

    // 문제 보상 삭제 전 문제 & 보상 존재하는지, 사용자 탈퇴 안하고 문제 작성자 맞는지 검사 1
    public Long getOverviewRewardIdBeforeRemoval(
            Long problemId, Long problemRewardId, Long authenticatedUserId
    ) {

        ProblemReward find = this.validateBeforeDeleteReward(
                problemId, problemRewardId, authenticatedUserId
        );

        return find.getOverviewRewardImage().getId();
    }

    // 문제 보상 삭제 전 문제 & 보상 존재하는지, 사용자 탈퇴 안하고 문제 작성자 맞는지 검사 2
    public Long getActualRewardIdBeforeRemoval(
            Long problemId, Long problemRewardId, Long authenticatedUserId
    ) {

        ProblemReward find = this.validateBeforeDeleteReward(
                problemId, problemRewardId, authenticatedUserId
        );

        return find.getActualRewardImage().getId();
    }

    // 문제 보상 삭제하기
    @Transactional
    public Long deleteReward(Long problemId, Long problemRewardId) {

        Problem problem = globalUtil.getNonSoftDeltedOrThrow(
                problemId, problemRepo::findById,
                () -> new UnexpectedNotFoundException(
                        "Expected to problem exists, but found nothing."
                )
        );

        ProblemReward find = problemRewardRepo.findById(problemRewardId)
                .orElseThrow(() -> new UnexpectedNotFoundException(
                        "Expected to reward exists, but found nothing."
                ));

        problemRewardRepo.delete(find);

        problem.decreaseNumOfRewardSets();

        return find.getId();
    }

    private ProblemReward validateBeforeDeleteReward(
            Long problemId, Long problemRewardId, Long userId
    ) {

        Long createdUserId = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        ).getUser().getId();

        ProblemReward find = problemRewardRepo.findById(problemRewardId)
                .orElseThrow(ProblemRewardNotFoundException::new);

        User user = this.findNonWithdrawnUserOrThrowUserNotFoundEx(
                userId
        );

        if (!createdUserId.equals(user.getId())) {
            throw new ForbiddenException("문제 작성자만 보상을 삭제할 수 있습니다.");
        }

        return find;
    }

    private Problem findNonSoftDeletedOrThrowProblemNotFoundEx(Long problemId) {
        return globalUtil.getNonSoftDeltedOrThrow(
                problemId, problemRepo::findById, ProblemNotFoundException::new
        );
    }

    private User findNonWithdrawnUserOrThrowUserNotFoundEx(Long userId) {
        return globalUtil.getNonSoftDeltedOrThrow(
                userId, userRepo::findById, UserNotFoundException::new
        );
    }

    private static class Utils {

        static ProblemRewardInfo toInfo(ProblemReward entity) {
            Long problemRewardId = entity.getId();
            String description = entity.getDescription();
            boolean hasTransfered = entity.hasTransferred();
            LocalDateTime createdAt = entity.getCreatedAt();

            return new ProblemRewardInfo(
                    problemRewardId, description,
                    hasTransfered, createdAt
            );
        }
    }
}
