package org;

import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.*;
import org.support.*;

@Configuration
@RequiredArgsConstructor
@EnableJpaRepositories(basePackageClasses = TestConfig.class, considerNestedRepositories = true)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
public class TestConfig {

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

    @Bean
    public GeneralDataInitializer generalDataInitializer() {
        return new GeneralDataInitializer(
                gainedRewardRepo, invitationRepo, notificationRepo, playRecordRepo,
                problemRepo, ratingRepo, receivedInvitationRepo, rewardRepo, scenarioRecordRepo,
                temporalProblemRepo, userRepo
        );
    }

}
