package org.app.user.service;

import java.time.*;
import java.util.function.*;
import lombok.*;
import org.app.config.domain.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.user.dto.request.*;
import org.app.user.repository.*;
import org.app.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class UserTemporalProblemService {

    private final GlobalUtil globalUtil;

    private final UserRepository userRepo;
    private final UserTemporalProblemRepository temporalProblemRepo;

    private final ScenarioInfoSerializer serializer;
    private final ScenarioInfoDeserializer deserializer;


    // 임시저장 목록보기
    public SimplePageResponse<SimpleTemporalProblemInfo> getMyTemporalProblems(
            Long userId, int pageNo, int pageSize
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<TemporalProblem> find = temporalProblemRepo.findByUserId(userId, pageable);

        return globalUtil.toSimplePageResponse(find, Util::toSimpleInfo);
    }

    // 임시저장 내용 보기
    public DetailedTemporalProblemInfo getMyTemporalProblem(
            Long userId, Long temporalId
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        TemporalProblem find = globalUtil.getOrThrow(
                temporalId, temporalProblemRepo::findById,
                TemporalProblemNotFoundException::new
        );

        if (!find.getUser().getId().equals(userId)) {
            throw new ForbiddenException("임시저장 내용은 자기 자신만 조회할 수 있습니다.");
        }

        String serializedScenarioInfo = find.getSerializedScenarioInfo();
        ScenarioInfo[] deserializedInfo = null;

        if (
                serializedScenarioInfo != null &&
                !serializedScenarioInfo.isEmpty()
        ) {
            deserializedInfo = deserializer.deserialize(serializedScenarioInfo);
        }

        return Util.toDetailedInfo(find, deserializedInfo);
    }

    // 임시저장 생성하기
    @Transactional
    public Long createTemporalProblem(Long userId, CreateTemporalProblemRequest request) {

        User user = this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        String title = request.title();
        String description = request.description();
        String rewardMsg = request.rewardMessage();
        Integer nOfSToGetReward = request.numOfScenariosToGetReward();
        Integer nOfSToFailPlay = request.numOfScenariosToFailPlay();
        ProblemVisibility visibility = request.visibility();
        String serializedInfo = this.getSerializedScenarioInfo(request.scenarioInfos());

        TemporalProblem newEntity = new TemporalProblem(
                user, title, description, rewardMsg,
                nOfSToGetReward, nOfSToFailPlay, visibility, serializedInfo
        );

        return temporalProblemRepo.save(newEntity).getId();
    }

    // 임시저장 수정하기
    @Transactional
    public Long updateTemporalProblem(
            Long userId, Long temporalId, CreateTemporalProblemRequest request
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        TemporalProblem find = globalUtil.getOrThrow(
                temporalId, temporalProblemRepo::findById,
                TemporalProblemNotFoundException::new
        );

        find
                .changeTitle(request.title())
                .changeDescription(request.description())
                .changeRewardMessage(request.rewardMessage())
                .changeNumOfScenariosToGetReward(request.numOfScenariosToGetReward())
                .changeNumOfScenariosToFailPlay(request.numOfScenariosToFailPlay())
                .changeVisibility(request.visibility())
                .changeSerializedScenarioInfo(
                        this.getSerializedScenarioInfo(request.scenarioInfos())
                );

        return find.getId();
    }

    // 임시저장 삭제하기
    @Transactional
    public Long deleteTemporalProblem(Long userId, Long temporalId) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        TemporalProblem find = globalUtil.getOrThrow(
                temporalId, temporalProblemRepo::findById,
                TemporalProblemNotFoundException::new
        );

        if (!find.getUser().getId().equals(userId)) {
            throw new ForbiddenException("임시저장 내용은 자기 자신만 삭제할 수 있습니다.");
        }

        temporalProblemRepo.delete(find);

        return temporalId;
    }

    private User findNonWithdrawnUserOrThrowUserNotFoundEx(Long userId) {
        return globalUtil.getOrThrow(
                userId, userRepo::findById, UserNotFoundException::new,
                Predicate.not(User::withdrawn)
        );
    }

    private String getSerializedScenarioInfo(ScenarioInfo[] scenarioInfos) {
        if (scenarioInfos == null || scenarioInfos.length == 0) {
            return null;
        }
        try {
            return serializer.serialize(scenarioInfos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize info due to: " + e.getCause(), e);
        }
    }

    private static class Util {

        static SimpleTemporalProblemInfo toSimpleInfo(TemporalProblem entity) {
            Long temporalId = entity.getId();
            String title = entity.getTitle();
            String description = entity.getDescription();
            String rewardMessage = entity.getRewardMessage();
            ProblemVisibility visibility = entity.getVisibility();
            LocalDateTime createdAt = entity.getCreatedAt();
            LocalDateTime modifiedAt = entity.getModifiedAt();

            return new SimpleTemporalProblemInfo(
                    temporalId, title, description,
                    rewardMessage, visibility, createdAt, modifiedAt
            );
        }

        static DetailedTemporalProblemInfo toDetailedInfo(
                TemporalProblem entity, ScenarioInfo[] scenarioInfos
        ) {
            SimpleTemporalProblemInfo simpleInfo = Util.toSimpleInfo(entity);

            Integer nOfSToGetReward = entity.getNumOfScenariosToGetReward();
            Integer nOfSToFailPlay = entity.getNumOfScenariosToFailPlay();
            int numOfTotalScenarios = scenarioInfos == null ? 0 : scenarioInfos.length;

            return new DetailedTemporalProblemInfo(
                    simpleInfo, nOfSToGetReward, nOfSToFailPlay,
                    numOfTotalScenarios, scenarioInfos
            );
        }
    }
}
