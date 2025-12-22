package org;

import lombok.*;
import org.app.config.domain.*;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.*;
import org.support.*;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackageClasses = TestConfig.class, considerNestedRepositories = true)
public class TestConfig {

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

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    private final SoftDeletePolicy softDeletePolicy;

    @Bean
    public GeneralDataInitializer generalDataInitializer() {
        return new GeneralDataInitializer(
                overviewRewardImageRepo, actualRewardImageRepo,
                gainedRewardRepo, invitationRepo, notificationRepo, playRecordRepo,
                problemRepo, problemAggregationRepo, ratingRepo, receivedInvitationRepo,
                problemRewardRepo, scenarioRecordRepo, temporalProblemRepo, userRepo,
                softDeletePolicy
        );
    }

}
