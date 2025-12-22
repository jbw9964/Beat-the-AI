package org.app.problem.service;

import java.time.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.config.domain.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.domain.search.filter.*;
import org.app.problem.domain.search.order.*;
import org.app.problem.dto.*;
import org.app.problem.repository.*;
import org.app.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SimpleProblemService {

    private final GlobalUtil globalUtil;

    private final ProblemRepository problemRepo;
    private final ProblemAggregationRepository problemAggregationRepo;
    private final ProblemUserRepository userRepo;

    private final ProblemInfoAccessAuthorizer accessAuthorizer;

    private final DateTimeProvider dateTimeProvider;
    private final SoftDeletePolicy softDeletePolicy;

    // 일반 공개 문제 목록 조회하기
    public SimplePageResponse<SimplePublicProblemInfo> getPublicProblems(
            int pageNo, int pageSize
    ) {
        Pageable pageable = globalUtil.pageable(pageNo, pageSize);
        Page<Problem> find = problemRepo.findPublicAndNonSoftDeletedProblemsFetchingAgg(pageable);

        return globalUtil.toSimplePageResponse(find, Utils::toSimpleInfo);
    }

    // 세부 공개 문제 검색하기
    public SimplePageResponse<SimplePublicProblemInfo> searchPublicProblems(
            List<ProblemFilter<?>> problemFilters, List<ProblemOrder> problemOrders,
            int pageNo, int pageSize
    ) {

        log.info(
                "Searching problems with: (pageNo={},pageSize={}) filters=[{}], orders=[{}]",
                pageNo, pageSize, problemFilters, problemOrders
        );

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<Problem> find = problemRepo.searchNonSoftDeletedPublicProblemWithFilters(
                problemFilters, problemOrders, pageable
        );

        return globalUtil.toSimplePageResponse(find, Utils::toSimpleInfo);
    }

    // 문제 정보 조회하기
    public DetailedProblemInfo getProblem(Long problemId, Long authenticatedUserId) {

        Problem find = problemRepo.findNonSoftDeletedProblemFetchingUserAndAgg(
                        problemId
                )
                .orElseThrow(ProblemNotFoundException::new);

        if (!accessAuthorizer.accessable(find, authenticatedUserId)) {
            throw new ForbiddenException("해당 문제에 접근할 권한이 없습니다.");
        }

        boolean isMine = find.getUser().getId()
                .equals(authenticatedUserId);

        return Utils.toDetailedInfo(find, isMine);
    }

    // 문제 생성하기
    @Transactional
    public Long createProblem(
            Long authenticatedUserId,
            SerializedProblemCreationInfo serializedProblemCreationInfo
    ) {

        User find = this.findNonWithdrawnUserOrThrowUserNotFoundEx(
                authenticatedUserId
        );

        String title = serializedProblemCreationInfo.title();
        String description = serializedProblemCreationInfo.description();
        String rewardMessage = serializedProblemCreationInfo.rewardMessage();
        int nOfSToGetReward = serializedProblemCreationInfo.numOfScenariosToGetReward();
        int nOfSToFailPlay = serializedProblemCreationInfo.numOfScenariosToFailPlay();
        ProblemVisibility visibility = serializedProblemCreationInfo.visibility();
        int numOfTotalScenarios = serializedProblemCreationInfo.numOfTotalScenarios();
        String serializedSInfo = serializedProblemCreationInfo.serializedScenarioInfo();

        Problem newProblem = new Problem(
                find, title, description, rewardMessage,
                nOfSToGetReward, nOfSToFailPlay,
                visibility, numOfTotalScenarios, serializedSInfo
        );

        newProblem = problemRepo.save(newProblem);

        if (visibility.equals(ProblemVisibility.PUBLIC)) {
            ProblemAggregation problemAggregation = new ProblemAggregation(newProblem);
            problemAggregationRepo.save(problemAggregation);
        }

        return newProblem.getId();
    }

    // 문제 수정하기
    @Transactional
    public Long updateProblem(
            Long problemId, Long authenticatedUserId,
            SerializedProblemUpdateInfo serializedProblemUpdateInfo
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(authenticatedUserId);

        Problem find = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        );

        if (!find.getUser().getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("문제 정보는 작성자만 수정할 수 있습니다.");
        }

        String title = serializedProblemUpdateInfo.title();
        String description = serializedProblemUpdateInfo.description();
        String rewardMessage = serializedProblemUpdateInfo.rewardMessage();
        int nOfSToGetReward = serializedProblemUpdateInfo.numOfScenariosToGetReward();
        int nOfSToFailPlay = serializedProblemUpdateInfo.numOfScenariosToFailPlay();
        int numOfTotalScenarios = serializedProblemUpdateInfo.numOfTotalScenarios();
        String serializedSInfo = serializedProblemUpdateInfo.serializedScenarioInfo();

        find
                .changeTitle(title)
                .changeDescription(description)
                .changeRewardMessage(rewardMessage)
                .changeNumOfScenariosToGetReward(nOfSToGetReward)
                .changeNumOfScenariosToFailPlay(nOfSToFailPlay)
                .changeSerializedScenarioInfo(
                        numOfTotalScenarios, serializedSInfo
                );

        return find.getId();
    }

    // 문제 공개 속성 바꾸기
    @Transactional
    @SuppressWarnings("LoggingSimilarMessage")
    public Long updateProblemVisibility(
            Long problemId, Long authenticatedUserId, ProblemVisibility changeTo
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(authenticatedUserId);

        Problem find = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        );

        if (!find.getUser().getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("문제 작성자만 공개 속성을 변경할 수 있습니다.");
        }

        ProblemVisibility origin = find.getVisibility();

        if (origin.equals(changeTo)) {
            return find.getId();
        }

        if (origin.equals(ProblemVisibility.PRIVATE)) {
            log.info(
                    "Creating new problem aggregation for problemId: {}",
                    problemId
            );

            problemAggregationRepo.save(new ProblemAggregation(find));

        } else if (origin.equals(ProblemVisibility.PUBLIC)) {
            log.info(
                    "Removing problem aggregation for problemId: {}",
                    problemId
            );

            Optional<ProblemAggregation> opt = problemAggregationRepo.findById(problemId);
            if (opt.isEmpty()) {
                log.warn(
                        "Expected problem aggregation exists with id={}, but found nothing. "
                        + "Skipping removal",
                        problemId
                );
            } else {
                ProblemAggregation findAgg = opt.get();
                findAgg.prepareAggregationRemoval();
                problemAggregationRepo.delete(findAgg);
            }
        }

        find.changeVisibility(changeTo);

        return find.getId();
    }

    // 문제 삭제하기
    @Transactional
    @SuppressWarnings("LoggingSimilarMessage")
    public Long deleteProblem(Long problemId, Long authenticatedUserId) {

        // TODO : 생각해 보니까 문제 삭제가 간단하지 않음.
        //  일단 연관된 rating 삭제부터 시작해서 초대 코드, 문제 설정된 보상도 삭제해야함.
        //  당연히 보상 삭제하면서 서버에 저장된 실제 보상들도 없애야 함.
        //  이건 확실히 제대로 생각해서 처리해야될듯. 아마 배치 처리가 가장 확실하지 않을듯 함.

        // TODO : 그럼 차라리 서버 이미지 저장하는 것 보다 DB 이미지 저장이 낫지 않나?
        //  어차피 배치 처리하고 서버에서 삭제해야 되는데, 그럴꺼면 차라리 acid 보장되는 db 에 저장하는게 낫지 않나?

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(authenticatedUserId);

        Problem find = this.findNonSoftDeletedOrThrowProblemNotFoundEx(
                problemId
        );

        if (!find.getUser().getId().equals(authenticatedUserId)) {
            throw new ForbiddenException("문제 작성자만 문제를 삭제할 수 있습니다.");
        }

        LocalDateTime now = dateTimeProvider.localDateTimeNow();
        LocalDate removalDate = softDeletePolicy.getRemovalDateOn(now);

        find.reserveRemoval(now, removalDate);

        ProblemVisibility visibility = find.getVisibility();
        if (visibility.equals(ProblemVisibility.PUBLIC)) {

            log.info(
                    "Public problem removal requested. "
                    + "Removing problem aggregation for problemId: {}",
                    problemId
            );

            Optional<ProblemAggregation> opt = problemAggregationRepo.findById(problemId);
            if (opt.isEmpty()) {
                log.warn(
                        "Expected problem aggregation exists with id={}, but found nothing. "
                        + "Skipping removal",
                        problemId
                );
            } else {
                ProblemAggregation findAgg = opt.get();
                findAgg.prepareAggregationRemoval();
                problemAggregationRepo.delete(findAgg);
            }
        }

        return find.getId();
    }

    private User findNonWithdrawnUserOrThrowUserNotFoundEx(Long userId) {
        return globalUtil.getNonSoftDeltedOrThrow(
                userId, userRepo::findById, UserNotFoundException::new
        );
    }

    private Problem findNonSoftDeletedOrThrowProblemNotFoundEx(Long problemId) {
        return globalUtil.getNonSoftDeltedOrThrow(
                problemId, problemRepo::findById, ProblemNotFoundException::new
        );
    }

    private static class Utils {

        static SimplePublicProblemInfo toSimpleInfo(Problem entity) {
            Long problemId = entity.getId();
            Long userId = entity.getUser().getId();
            String title = entity.getTitle();
            String description = entity.getDescription();
            int numOfScenariosToGetReward = entity.getNumOfScenariosToGetReward();
            int numOfScenariosToFailPlay = entity.getNumOfScenariosToFailPlay();
            int numOfTotalScenarios = entity.getNumOfTotalScenarios();
            int numOfRewardSets = entity.getNumOfRewardSets();
            AggregatedInfo aggInfo = toRecord(entity.getProblemAggregation());
            LocalDateTime createdAt = entity.getCreatedAt();
            LocalDateTime modifiedAt = entity.getModifiedAt();

            return new SimplePublicProblemInfo(
                    problemId, userId, title, description,
                    numOfScenariosToGetReward, numOfScenariosToFailPlay,
                    numOfTotalScenarios, numOfRewardSets, aggInfo,
                    createdAt, modifiedAt
            );
        }

        static DetailedProblemInfo toDetailedInfo(
                Problem entity, boolean isMine
        ) {
            SimplePublicProblemInfo simpleInfo = toSimpleInfo(entity);

            String userName = entity.getUser().getName();
            ProblemVisibility visibility = entity.getVisibility();

            return new DetailedProblemInfo(simpleInfo, userName, visibility, isMine);
        }

        private static AggregatedInfo toRecord(
                ProblemAggregation aggEntity
        ) {
            if (aggEntity == null) {
                return null;
            }

            PlayInfo playInfo = toRecord(aggEntity.getPlayInfo());
            RatingInfo ratingInfo = toRecord(aggEntity.getRatingInfo());

            return new AggregatedInfo(playInfo, ratingInfo);
        }

        private static PlayInfo toRecord(AggregatedProblemPlayInfo playInfo) {
            return new PlayInfo(playInfo.getNumOfTotalPlays());
        }

        private static RatingInfo toRecord(AggregatedProblemRatingInfo ratingInfo) {
            return new RatingInfo(
                    ratingInfo.getNumOfTotalRatings(),
                    ratingInfo.getRatingAverage()
            );
        }
    }
}
