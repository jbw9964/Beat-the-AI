package org.app.user.service;

import java.time.*;
import java.util.*;
import lombok.*;
import org.app.config.domain.*;
import org.app.config.domain.scenario.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.user.dto.response.*;
import org.app.user.event.*;
import org.app.user.repository.*;
import org.app.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.springframework.context.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

@Service
@RequiredArgsConstructor
public class SimpleUserService {

    private final GlobalUtil globalUtil;
    private final SoftDeletePolicy softDeletePolicy;
    private final DateTimeProvider dateTimeProvider;
    private final ApplicationEventPublisher eventPublisher;

    private final UserRepository userRepo;
    private final PasswordEncoder pwEncoder;

    private final UserProblemRepository problemRepo;
    private final ScenarioInfoDeserializer scenarioInfoDeserializer;

    private final UserRatingRepository ratingRepo;

    // 자기 정보 보기
    public GetUserResponse getMe(Long userId) {

        User find = this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        String username = find.getName();
        String email = find.getEmail();
        String thumbnailUrl = find.getThumbnailUrl();

        return new GetUserResponse(userId, username, email, thumbnailUrl, true);
    }

    // 회원탈퇴하기
    @Transactional
    public Long withdrawMe(Long userId) {
        // TODO : 이벤트 기반 유저 자원 삭제 필요.
        //  일단 withdraw 속성으로 조회 안되게 만들었고,
        //  이후 다른부분 개발하면서 다른 삭제시키는거 만들어야됨.
        //  아님 batch 처리로 일정 기한 넘어가면 다 삭제시키거나.

        // TODO : 생각해보니 유저 삭제도 간단하지 않음.
        //  유저 삭제하려면 관련 문제도 삭제시키고 DB 저장된 보상들도 다 삭제해야함.
        //  처음엔 단순 이벤트 기반으로도 가능할 거라 생각했는데 뭔가 batch 처리 해야할 것 같음.

        User find = this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        LocalDateTime now = dateTimeProvider.localDateTimeNow();
        LocalDate removalDate = softDeletePolicy.getRemovalDateOn(now);
        find.withdrawUser(now, removalDate);

        eventPublisher.publishEvent(new UserWithdrawEvent(userId));

        return userId;
    }

    // 내 정보 수정하기 : 이름, 이메일, 썸네일 등
    @Transactional
    public Long updateMyInfo(
            Long userId, String newUsername, String newEmail, String newThumbnail
    ) {

        User find = this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        find.changeName(newUsername);
        find.changeEmail(newEmail);
        find.changeThumbnailUrl(newThumbnail);

        return userId;
    }

    // 내 설정 수정하기 : 알림 설정, 플레이시 default visibility 등 (상세하게 생각 아직 안함)
    @Transactional
    public Long updateMySetting(Long userId) {
        // TODO : 설정 수정 구현
        User find = this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        throw new NotImplementedException("설정 수정 미구현");
    }

    // 비번 바꾸기
    @Transactional
    public Long updateMyPassword(
            Long userId, String oldPassword, String newPassword
    ) {

        User find = this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        String encryptedPassword = find.getEncryptedPassword();
        if (!pwEncoder.matches(oldPassword, encryptedPassword)) {
            throw new PasswordMismatchException("패스워드가 일치하지 않아 비밀번호를 변경할 수 없습니다.");
        }

        String newEncryptedPassword = pwEncoder.encode(newPassword);
        find.changeEncryptedPassword(newEncryptedPassword);

        return userId;
    }

    // 내가 만든 문제 목록 보기
    public SimplePageResponse<SimpleProblemInfo> getMyProblems(
            Long userId, int pageNo, int pageSize
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        Pageable pageable = globalUtil.pageable(pageNo, pageSize);
        Page<Problem> find = problemRepo.findNonSoftDeletedProblemsByUserId(userId, pageable);

        return globalUtil.toSimplePageResponse(find, Util::toSimpleInfo);
    }

    // 내가 만든 문제 내용 보기
    public DetailedProblemInfo getMyProblem(Long userId, Long problemId) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        Problem find = globalUtil.getNonSoftDeltedOrThrow(
                problemId, problemRepo::findById, ProblemNotFoundException::new
        );

        if (!find.getUser().getId().equals(userId)) {
            throw new ForbiddenException("자신이 만든 문제만 조회할 수 있습니다.");
        }

        String serialized = find.getSerializedScenarioInfo();
        ScenarioInfo[] deserialized = scenarioInfoDeserializer.deserialize(serialized);

        return Util.toDetailedInfo(find, deserialized);
    }

    // 내가 평가한 내용 목록 보기
    public SimplePageResponse<RatingInfo> getMyRatings(
            Long userId, int pageNo, int pageSize
    ) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        Pageable pageable = globalUtil.pageable(pageNo, pageSize);
        Page<Rating> find = ratingRepo.findByUserId(userId, pageable);

        return globalUtil.toSimplePageResponse(find, Util::toInfo);
    }

    // 내가 평가한 내용 보기
    public RatingInfo getMyRating(Long userId, Long ratingId) {

        this.findNonWithdrawnUserOrThrowUserNotFoundEx(userId);

        Rating find = ratingRepo.findById(ratingId)
                .orElseThrow(RatingNotFoundException::new);

        if (!find.getUserId().equals(userId)) {
            throw new ForbiddenException("자신이 작성한 평가 내용만 볼 수 있습니다.");
        }

        return Util.toInfo(find);
    }

    private User findNonWithdrawnUserOrThrowUserNotFoundEx(Long userId) {
        return globalUtil.getNonSoftDeltedOrThrow(
                userId, userRepo::findById, UserNotFoundException::new
        );
    }

    private record Util() {

        static SimpleProblemInfo toSimpleInfo(Problem entity) {
            Long problemId = entity.getId();
            String title = entity.getTitle();
            String description = entity.getDescription();
            String rewardMessage = entity.getRewardMessage();
            int numOfScenariosToGetReward = entity.getNumOfScenariosToGetReward();
            int numOfScenariosToFailPlay = entity.getNumOfScenariosToFailPlay();
            ProblemVisibility visibility = entity.getVisibility();
            LocalDateTime createdAt = entity.getCreatedAt();
            LocalDateTime modifiedAt = entity.getModifiedAt();

            return new SimpleProblemInfo(
                    problemId, title, description, rewardMessage,
                    numOfScenariosToGetReward, numOfScenariosToFailPlay,
                    visibility, createdAt, modifiedAt
            );
        }

        static DetailedProblemInfo toDetailedInfo(
                Problem entity, ScenarioInfo[] scenarioInfos
        ) {
            SimpleProblemInfo simpleInfo = toSimpleInfo(entity);

            return new DetailedProblemInfo(simpleInfo, Arrays.asList(scenarioInfos));
        }

        static RatingInfo toInfo(Rating entity) {
            Long ratingId = entity.getId();
            Long problemId = entity.getProblem().getId();
            String comment = entity.getComment();
            int score = entity.getScore();
            LocalDateTime createdAt = entity.getCreatedAt();
            LocalDateTime modifiedAt = entity.getModifiedAt();

            return new RatingInfo(ratingId, problemId, comment, score, createdAt, modifiedAt);
        }
    }
}
