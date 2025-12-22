package org.support;

import java.time.*;
import lombok.*;
import org.app.config.domain.*;
import org.app.entity.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.transaction.annotation.*;

@Transactional
@RequiredArgsConstructor
@SuppressWarnings("unused")
public class GeneralDataInitializer {

    private static final NotificationSetting setting = null;

    private final TestOverviewRewardImageRepository overviewRewardImageRepo;
    private final TestActualRewardImageRepository actualRewardImageRepo;

    private final TestGainedRewardRepository gainedRewardRepo;
    private final TestInvitationRepository invitationRepo;
    private final TestNotificationRepository notificationRepo;
    private final TestPlayRecordRepository playRecordRepo;
    private final TestProblemRepository problemRepo;
    private final TestProblemAggregationRepository problemAggregationRepo;
    private final TestRatingRepository ratingRepo;
    private final TestReceivedInvitationRepository receivedInvitationRepo;
    private final TestProblemRewardRepository problemRewardRepo;
    private final TestScenarioRecordRepository scenarioRecordRepo;
    private final TestTemporalProblemRepository temporalProblemRepo;
    private final TestUserRepository userRepo;

    private final SoftDeletePolicy softDeletePolicy;

    @Builder(builderMethodName = "actualRewardImageBuilder")
    public ActualRewardImage createActualRewardImage(
            RewardStorageType storageType,
            byte[] actualImage, String actualImagePath
    ) {
        ActualRewardImage actualRewardImage;

        switch (storageType) {
            case DB -> actualRewardImage
                    = new DbStorageActualRewardImage(actualImage);
            case SERVER -> actualRewardImage
                    = new ServerStorageActualRewardImage(actualImagePath);
            case AWS_S3 -> actualRewardImage
                    = new AwsS3ActualRewardImage(actualImagePath);
            default -> throw new IllegalArgumentException("Unsupported storage type");
        }

        return actualRewardImageRepo.save(actualRewardImage);
    }

    @Builder(builderMethodName = "overviewRewardImageBuilder")
    public OverviewRewardImage createOverviewRewardImage(
            RewardStorageType storageType,
            byte[] overviewImage, String overviewImagePath
    ) {
        OverviewRewardImage overviewRewardImage;

        switch (storageType) {
            case DB -> overviewRewardImage
                    = new DbStorageOverviewRewardImage(overviewImage);
            case SERVER -> overviewRewardImage
                    = new ServerStorageOverviewRewardImage(overviewImagePath);
            case AWS_S3 -> overviewRewardImage
                    = new AwsS3OverviewRewardImage(overviewImagePath);
            default -> throw new IllegalArgumentException("Unsupported storage type");
        }

        return overviewRewardImageRepo.save(overviewRewardImage);
    }

    @Builder(builderMethodName = "gainedRewardBuilder")
    public GainedReward createGainedReward(
            Long userId, Long playRecordId, Long actualRewardImageId,
            Long rewardId, String description
    ) {
        User findUser = userRepo.findById(userId).orElseThrow(AssertionError::new);
        PlayRecord findRecord = playRecordRepo.findById(playRecordId)
                .orElseThrow(AssertionError::new);
        ActualRewardImage findActualImg = actualRewardImageRepo.findById(actualRewardImageId)
                .orElseThrow(AssertionError::new);

        GainedReward gainedReward = new GainedReward(
                findUser, findRecord, rewardId, description,
                findActualImg
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

    @Builder(builderMethodName = "userBuilder")
    public User createUser(
            String name, String email, String loginId,
            String encryptedPassword, String thumbnail,
            boolean withdrawn, LocalDateTime withdrawnAt
    ) {
        User user = new User(name, loginId, encryptedPassword);
        user.changeEmail(email);
        user.changeThumbnailUrl(thumbnail);
        if (withdrawn) {
            LocalDate removalDate = softDeletePolicy.getRemovalDateOn(withdrawnAt);
            user.withdrawUser(withdrawnAt, removalDate);
        }
        return userRepo.save(user);
    }

    @Builder(builderMethodName = "receivedInvitationBuilder")
    public ReceivedInvitation createReceivedInvitation(
            Long userId, Long problemId, String title, String code
    ) {
        User find = userRepo.findById(userId).orElseThrow(AssertionError::new);
        ReceivedInvitation receivedInvitation = new ReceivedInvitation(find, problemId, title,
                code);
        return receivedInvitationRepo.save(receivedInvitation);
    }

    @Builder(builderMethodName = "playRecordBuilder")
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

    @Builder(builderMethodName = "problemRewardBuilder")
    public ProblemReward createProblemReward(
            Long problemId, Long actualRewardImageId,
            Long overviewRewardImageId, String description,
            boolean hasTransferred
    ) {
        Problem find = problemRepo.findById(problemId).orElseThrow(AssertionError::new);
        OverviewRewardImage findOverviewImg = overviewRewardImageRepo.findById(
                        overviewRewardImageId)
                .orElseThrow(AssertionError::new);
        ActualRewardImage findActualImg = actualRewardImageRepo.findById(actualRewardImageId)
                .orElseThrow(AssertionError::new);

        ProblemReward problemReward = new ProblemReward(
                find, description, hasTransferred,
                findOverviewImg, findActualImg
        );
        return problemRewardRepo.save(problemReward);
    }

    @Builder(builderMethodName = "scenarioRecordBuilder")
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

    @Builder(builderMethodName = "problemBuilder")
    public Problem createProblem(
            Long userId, String title, String description, String rewardMessage,
            int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
            ProblemVisibility visibility, int numOfTotalScenarios, String serializedScenarioInfo,
            int numOfRewardSets
    ) {
        User find = userRepo.findById(userId).orElseThrow(AssertionError::new);

        Problem problem = new Problem(
                find, title, description, rewardMessage,
                numOfScenariosToGetReward, numOfScenariosToFailPlay,
                visibility, numOfTotalScenarios, serializedScenarioInfo, numOfRewardSets
        );

        return problemRepo.save(problem);
    }

    @Builder(builderMethodName = "problemAggregationBuilder")
    public ProblemAggregation createProblemAggregation(
            Long problemId, AggregatedProblemRatingInfo ratingInfo,
            AggregatedProblemPlayInfo playInfo
    ) {
        Problem find = problemRepo.findById(problemId).orElseThrow(AssertionError::new);

        ProblemAggregation problemAggregation = new ProblemAggregation(
                find, playInfo, ratingInfo
        );

        return problemAggregationRepo.save(problemAggregation);
    }

    @Builder(builderMethodName = "temporalProblemBuilder")
    public TemporalProblem createTemporalProblem(
            Long userId, String title, String description, String rewardMessage,
            Integer numOfScenariosToGetReward, Integer numOfScenariosToFailPlay,
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

    @Builder(builderMethodName = "ratingBuilder")
    public Rating createRating(Long problemId, Long userId, String comment, int score) {
        Problem find = problemRepo.findById(problemId).orElseThrow(AssertionError::new);
        Rating rating = new Rating(find, userId, comment, score);
        return ratingRepo.save(rating);
    }

    @Builder(builderMethodName = "invitationBuilder")
    public Invitation createInvitation(Long problemId, String code) {
        Problem find = problemRepo.findById(problemId).orElseThrow(AssertionError::new);
        Invitation invitation = new Invitation(find, code);
        return invitationRepo.save(invitation);
    }

    public void initAll() {
        initProblemDomain();

        initPlayDomain();

        initUserDomain();

        initRewards();
    }

    private void initProblemDomain() {
        this.deleteAll(invitationRepo);
        this.deleteAll(problemRewardRepo);
        this.deleteAll(ratingRepo);
        this.deleteAll(problemAggregationRepo);
        this.deleteAll(problemRepo);
    }

    private void initPlayDomain() {
        this.deleteAll(gainedRewardRepo);
        this.deleteAll(scenarioRecordRepo);
        this.deleteAll(playRecordRepo);
    }

    private void initUserDomain() {
        this.deleteAll(notificationRepo);
        this.deleteAll(temporalProblemRepo);
        this.deleteAll(receivedInvitationRepo);
        this.deleteAll(userRepo);
    }

    private void initRewards() {
        this.deleteAll(actualRewardImageRepo);
        this.deleteAll(overviewRewardImageRepo);
    }

    private <E, I> void deleteAll(
            JpaRepository<E, I> jpaRepo
    ) {
        jpaRepo.deleteAllInBatch();
    }
}
