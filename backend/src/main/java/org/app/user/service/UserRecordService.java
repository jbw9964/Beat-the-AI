package org.app.user.service;

import java.time.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.user.dto.response.*;
import org.app.user.repository.*;
import org.app.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRecordService {

    private final GlobalUtil globalUtil;

    private final UserRepository userRepo;
    private final UserPlayRecordRepository playRecordRepo;

    private final UserGainedRewardRepository gainedRewardRepo;

    // 플레이 기록 목록 보기
    public SimplePageResponse<SimplePlayRecordInfo> getMyRecords(
            Long userId, int pageNo, int pageSize
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<PlayRecord> find = playRecordRepo.findByUserId(userId, pageable);

        return globalUtil.toSimplePageResponse(find, Util::toSimpleInfo);
    }

    // 플레이 내용 보기
    public GetMyRecordResponse getMyRecord(Long userId, Long playRecordId) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        PlayRecord find = globalUtil.getOrThrow(
                playRecordId, playRecordRepo::findByIdFetchingScenarioRecords,
                PlayRecordNotFoundException::new
        );

        if (!find.getUser().getId().equals(userId)) {
            throw new ForbiddenException("자기 자신만 조회할 수 있습니다.");
        }

        DetailedPlayRecordInfo detailedPlayRecordInfo = Util.toDetailedInfo(find);
        List<ScenarioRecordInfo> submittedScenarioInfos
                = find.getScenarioRecords().stream()
                .filter(ScenarioRecord::hasSubmitted)
                .map(Util::toScenarioInfo)
                .sorted(Comparator.comparing(ScenarioRecordInfo::scenarioOrder))
                .toList();

        return new GetMyRecordResponse(detailedPlayRecordInfo, submittedScenarioInfos);
    }

    // 플레이 기록 공개 속성 바꾸기
    @Transactional
    public Long changeMyRecordVisibility(
            Long userId, Long playRecordId, PlayRecordVisibility visibility
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        PlayRecord find = globalUtil.getOrThrow(
                playRecordId, playRecordRepo::findById,
                PlayRecordNotFoundException::new
        );

        if (!find.getUser().getId().equals(userId)) {
            throw new ForbiddenException("자기 자신만 공개 여부를 변경할 수 있습니다.");
        }

        if (find.getVisibility().equals(visibility)) {
            log.warn("Given visibility are same. No modification will occur.");
        } else {
            find.changeVisibility(visibility);
        }

        return find.getId();
    }

    // 플레이 기록과 연관된 보상 목록 보기
    public SimplePageResponse<GainedRewardInfo> getMyRewards(
            Long userId, Long playRecordId, int pageNo, int pageSize
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);
        PlayRecord playRecord = globalUtil.getOrThrow(
                playRecordId, playRecordRepo::findById,
                PlayRecordNotFoundException::new
        );

        if (!playRecord.getUser().getId().equals(userId)) {
            throw new ForbiddenException("보상 목록은 자기 자신만 조회할 수 있습니다.");
        }

        if (!playRecord.hasCleared()) {
            throw new NonClearedPlayRecordException(
                    "오직 성공한 기록의 보상만 조회할 수 있습니다."
            );
        }

        Pageable pageable = PageRequest.of(pageNo, pageSize);

        Page<GainedReward> find = gainedRewardRepo.findByPlayRecordId(playRecordId, pageable);

        return globalUtil.toSimplePageResponse(find, Util::toInfo);
    }

    // 플레이 기록과 연관된 보상 정보 보기 (실 이미지 전달 X)
    public GainedRewardInfo getMyReward(Long userId, Long playRecordId, Long gainedRewardId) {
        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);
        PlayRecord playRecord = globalUtil.getOrThrow(
                playRecordId, playRecordRepo::findById,
                PlayRecordNotFoundException::new
        );

        if (!playRecord.getUser().getId().equals(userId)) {
            throw new ForbiddenException("플레이 기록이 자기 자신의 것이 아닙니다.");
        }

        if (!playRecord.hasCleared()) {
            throw new NonClearedPlayRecordException(
                    "오직 성공한 기록의 보상만 조회할 수 있습니다."
            );
        }

        GainedReward find = globalUtil.getOrThrow(
                gainedRewardId, gainedRewardRepo::findById,
                GainedRewardNotFoundException::new
        );

        if (!find.getPlayRecord().getId().equals(playRecordId)) {
            throw new ForbiddenException("해당 보상은 다른 플레이 기록과 연관된 보상입니다.");
        }

        if (!find.getUser().getId().equals(userId)) {
            throw new ForbiddenException("보상 내용은 자기 자신만 조회할 수 있습니다.");
        }

        return Util.toInfo(find);
    }

    // 플레이 기록과 연관된 모든 보상들 삭제하기
    @Transactional
    public DeleteMyRewardResponse deleteMyRewards(Long userId, Long playRecordId) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        PlayRecord playRecord = globalUtil.getOrThrow(
                playRecordId, playRecordRepo::findById,
                PlayRecordNotFoundException::new
        );

        if (!playRecord.getUser().getId().equals(userId)) {
            throw new ForbiddenException("플레이 기록이 자기 자신의 것이 아닙니다.");
        }

        List<GainedReward> finds = gainedRewardRepo.findAllByPlayRecordId(playRecordId);
        List<Long> ids = finds.stream()
                .map(GainedReward::getId)
                .toList();

        log.warn("Executing bulk delete");
        gainedRewardRepo.deleteAllByIds(ids);

        return new DeleteMyRewardResponse(ids);
    }

    private void findNonWithdrawnUserOrThrowUserNotFoundEx(Long userId) {
        globalUtil.getNonSoftDeltedOrThrow(
                userId, userRepo::findById, UserNotFoundException::new
        );
    }

    @SuppressWarnings("DuplicatedCode")
    private static class Util {

        static SimplePlayRecordInfo toSimpleInfo(PlayRecord entity) {
            Long playRecordId = entity.getId();
            Long problemId = entity.getProblemId();
            String title = entity.getTitle();
            String description = entity.getDescription();
            PlayRecordStatus status = entity.getStatus();
            PlayRecordVisibility visibility = entity.getVisibility();
            LocalDateTime createdAt = entity.getCreatedAt();

            return new SimplePlayRecordInfo(
                    playRecordId, problemId, title, description, status, visibility, createdAt
            );
        }

        static DetailedPlayRecordInfo toDetailedInfo(PlayRecord entity) {
            SimplePlayRecordInfo simpleInfo = Util.toSimpleInfo(entity);

            List<ScenarioRecord> scenarioRecords = entity.getScenarioRecords();
            int nOfTotalSce = scenarioRecords.size();
            int nOfSubmittedSce = 0;
            int nOfPassedSce = 0;

            for (ScenarioRecord sr : scenarioRecords) {
                if (sr.hasSubmitted()) {
                    nOfSubmittedSce++;
                }
                if (sr.hasPassed()) {
                    nOfPassedSce++;
                }
            }

            int nOfSceToGetReward = entity.getNumOfScenariosToGetReward();
            int nOfSceToFailPlay = entity.getNumOfScenariosToFailPlay();

            return new DetailedPlayRecordInfo(
                    simpleInfo, nOfTotalSce, nOfSubmittedSce, nOfPassedSce,
                    nOfSceToGetReward, nOfSceToFailPlay
            );
        }

        static ScenarioRecordInfo toScenarioInfo(ScenarioRecord entity) {
            Long scenarioRecordId = entity.getId();
            int scenarioOrder = entity.getScenarioOrder();
            String scenarioContent = entity.getScenarioContent();
            String userSubmissionContent = entity.getUserSubmissionContent();
            String aiGeneratedContent = entity.getAiGeneratedContent();
            boolean hasSubmitted = entity.hasSubmitted();
            boolean hasPassed = entity.hasPassed();
            LocalDateTime submittedAt = entity.getSubmittedAt();

            return new ScenarioRecordInfo(
                    scenarioRecordId, scenarioOrder, scenarioContent,
                    userSubmissionContent, aiGeneratedContent,
                    hasSubmitted, hasPassed, submittedAt
            );
        }

        static GainedRewardInfo toInfo(GainedReward entity) {
            Long gainedRewardId = entity.getId();
            Long playRecordId = entity.getPlayRecord().getId();
            String description = entity.getDescription();
            LocalDateTime createdAt = entity.getCreatedAt();

            return new GainedRewardInfo(gainedRewardId, playRecordId, description, createdAt);
        }
    }
}
