package org.app.user.service;

import java.time.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.user.dto.response.*;
import org.app.user.repository.*;
import org.app.util.*;
import org.app.util.api.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.*;

@Service
@RequiredArgsConstructor
public class AnonymousUserService {

    private final GlobalUtil globalUtil;
    private final UserRepository userRepo;
    private final UserPlayRecordRepository playRecordRepo;

    public GetUserResponse getUser(Long userId, Long authenticatedUserId) {

        User find = globalUtil.getOrThrow(
                userId, userRepo::findById, UserNotFoundException::new,
                Predicate.not(User::withdrawn)
        );

        String username = find.getName();
        String email = find.getEmail();
        String thumbnailUrl = find.getThumbnailUrl();
        boolean isMine = userId.equals(authenticatedUserId);

        return new GetUserResponse(userId, username, email, thumbnailUrl, isMine);
    }


    public GetPublicRecordsResponse getPublicRecords(
            Long userId, int pageNo, int pageSize, Long authenticatedUserId
    ) {

        globalUtil.getOrThrow(
                userId, userRepo::findById, UserNotFoundException::new,
                Predicate.not(User::withdrawn)
        );

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        Page<PlayRecord> find
                = playRecordRepo.findPublicRecordsByUserId(userId, pageable);

        SimplePageResponse<SimplePlayRecordInfo> pageResponse
                = globalUtil.toSimplePageResponse(find, Util::toSimpleInfo);
        boolean isMine = userId.equals(authenticatedUserId);

        return new GetPublicRecordsResponse(pageResponse, isMine);
    }

    public GetPublicRecordResponse getPublicRecord(
            Long userId, Long recordId, Long authenticatedUserId
    ) {

        globalUtil.getOrThrow(
                userId, userRepo::findById, UserNotFoundException::new,
                Predicate.not(User::withdrawn)
        );

        PlayRecord find = globalUtil.getOrThrow(
                recordId, playRecordRepo::findPublicRecordsByIdFetchingScenarioRecords,
                PublicPlayRecordNotFoundException::new
        );

        DetailedPlayRecordInfo detailedPlayRecordInfo = Util.toDetailedInfo(find);
        List<ScenarioRecordInfo> submittedScenarioInfos
                = find.getScenarioRecords().stream()
                .filter(ScenarioRecord::hasSubmitted)
                .map(Util::toScenarioInfo)
                .sorted(Comparator.comparing(ScenarioRecordInfo::scenarioOrder))
                .toList();
        SimplePageResponse<ScenarioRecordInfo> scenarioPageResponse =
                new SimplePageResponse<>(submittedScenarioInfos);
        boolean isMine = userId.equals(authenticatedUserId);

        return new GetPublicRecordResponse(
                detailedPlayRecordInfo, scenarioPageResponse, isMine
        );
    }

    private record Util() {

        static SimplePlayRecordInfo toSimpleInfo(PlayRecord entity) {
            Long playRecordId = entity.getId();
            Long problemId = entity.getProblemId();
            String title = entity.getTitle();
            String description = entity.getDescription();
            PlayRecordStatus status = entity.getStatus();
            PlayRecordVisibility visibility = entity.getVisibility();
            LocalDateTime createdAt = entity.getCreatedAt();

            return new SimplePlayRecordInfo(
                    playRecordId, problemId, title, description,
                    status, visibility, createdAt
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
    }
}
