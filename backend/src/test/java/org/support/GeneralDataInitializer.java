package org.support;

import java.time.*;
import lombok.*;
import org.app.entity.*;
import org.springframework.transaction.annotation.*;

@Transactional
@RequiredArgsConstructor
public class GeneralDataInitializer {

    private static final NotificationSetting setting = null;
    private final TestGainedRewardRepository gainedRewardRepo;
    private final TestInvitationRepository invitationRepo;
    private final TestNotificationRepository notificationRepo;
    private final TestPlayRecordRepository playRecordRepo;
    private final TestProblemRepository problemRepo;
    private final TestRatingRepository ratingRepo;
    private final TestReceivedInvitationRepository receivedInvitationRepo;
    private final TestRewardRepository rewardRepo;
    private final TestScenarioRecordRepository scenarioRecordRepo;
    private final TestTemporalProblemRepository temporalProblemRepo;

    private final TestUserRepository userRepo;

    public GainedReward createGainedReward(
            Long userId, Long playRecordId, Long rewardId,
            String description, String location,
            RewardStorageType storageType
    ) {
        User findUser = userRepo.findById(userId).orElseThrow(AssertionError::new);
        PlayRecord findRecord = playRecordRepo.findById(playRecordId)
                .orElseThrow(AssertionError::new);
        GainedReward gainedReward = new GainedReward(
                findUser, findRecord, rewardId, description, location, storageType
        );
        return gainedRewardRepo.save(gainedReward);
    }

    public Notification createNotification(
            Long userId, String overview, String description
    ) {
        User find = userRepo.findById(userId).orElseThrow(AssertionError::new);
        Notification notification = new Notification(find, overview, description);
        return notificationRepo.save(notification);
    }

    public User createUser(
            String name, String email, String loginId,
            String encryptedPassword, String thumbnail,
            boolean withdrawn, LocalDate withdrawnAt
    ) {
        User user = new User(name, loginId, encryptedPassword);
        user.changeEmail(email);
        user.changeThumbnailUrl(thumbnail);
        if (withdrawn) {
            user.withdrawUser(withdrawnAt);
        }
        return userRepo.save(user);
    }

    public ReceivedInvitation createReceivedInvitation(
            Long userId, Long problemId, String title, String code
    ) {
        User find = userRepo.findById(userId).orElseThrow(AssertionError::new);
        ReceivedInvitation receivedInvitation = new ReceivedInvitation(find, problemId, title,
                code);
        return receivedInvitationRepo.save(receivedInvitation);
    }

    public PlayRecord createPlayRecord(
            Long userId, Long problemId, String title, String description,
            String rewardMessage, int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
            PlayRecordStatus status, PlayRecordVisibility visibility
    ) {
        User find = userRepo.findById(userId).orElseThrow(AssertionError::new);
        PlayRecord playRecord = new PlayRecord(
                find, problemId, title, description, rewardMessage, numOfScenariosToGetReward,
                numOfScenariosToFailPlay, status, visibility
        );
        return playRecordRepo.save(playRecord);
    }

    public Reward createReward(
            Long problemId, String description, String originLocation,
            String overviewLocation,
            RewardStorageType storageType, boolean hasTransferred
    ) {
        Problem find = problemRepo.findById(problemId).orElseThrow(AssertionError::new);
        Reward reward = new Reward(
                find, description, originLocation, overviewLocation,
                storageType, hasTransferred
        );
        return rewardRepo.save(reward);
    }

    public ScenarioRecord createScenarioRecord(
            Long playRecordId, int scenarioOrder, String scenarioContent,
            boolean hasSubmitted, String userSubmissionContent, String aiGeneratedContent,
            boolean hasPassed, LocalDateTime submittedAt
    ) {
        PlayRecord find = playRecordRepo.findById(playRecordId).orElseThrow(AssertionError::new);
        ScenarioRecord scenarioRecord = new ScenarioRecord(find, scenarioOrder, scenarioContent);

        if (hasSubmitted) {
            scenarioRecord.updateSubmission(
                    userSubmissionContent, aiGeneratedContent, hasPassed, submittedAt
            );
        }

        return scenarioRecordRepo.save(scenarioRecord);
    }

    public Problem createProblem(
            Long userId, String title, String description, String rewardMessage,
            int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
            ProblemVisibility visibility, String serializedScenarioInfo
    ) {
        User find = userRepo.findById(userId).orElseThrow(AssertionError::new);

        Problem problem = new Problem(
                find, title, numOfScenariosToGetReward, numOfScenariosToFailPlay,
                visibility, serializedScenarioInfo
        );

        problem.changeDescription(description);
        problem.changeRewardMessage(rewardMessage);

        return problemRepo.save(problem);
    }

    public TemporalProblem createTemporalProblem(
            Long userId, String title, String description, String rewardMessage,
            int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
            ProblemVisibility visibility, String serializedScenarioInfo
    ) {
        User find = userRepo.findById(userId).orElseThrow(AssertionError::new);
        TemporalProblem temporalProblem = new TemporalProblem(
                find, title, description, rewardMessage,
                numOfScenariosToGetReward, numOfScenariosToFailPlay,
                visibility, serializedScenarioInfo
        );
        return temporalProblemRepo.save(temporalProblem);
    }

    public Rating createRating(Long problemId, Long userId, String comment, int score) {
        Problem find = problemRepo.findById(problemId).orElseThrow(AssertionError::new);
        Rating rating = new Rating(find, userId, comment, score);
        return ratingRepo.save(rating);
    }

    public Invitation createInvitation(Long problemId, String code) {
        Problem find = problemRepo.findById(problemId).orElseThrow(AssertionError::new);
        Invitation invitation = new Invitation(find, code);
        return invitationRepo.save(invitation);
    }

    public void initAll() {
        initProblemDomain();

        initPlayDomain();

        initUserDomain();
    }

    private void initProblemDomain() {
        invitationRepo.deleteAll();
        rewardRepo.deleteAll();
        ratingRepo.deleteAll();
        problemRepo.deleteAll();
    }

    private void initPlayDomain() {
        gainedRewardRepo.deleteAll();
        scenarioRecordRepo.deleteAll();
        playRecordRepo.deleteAll();
    }

    private void initUserDomain() {
        notificationRepo.deleteAll();
        temporalProblemRepo.deleteAll();
        receivedInvitationRepo.deleteAll();
        userRepo.deleteAll();
    }
}
