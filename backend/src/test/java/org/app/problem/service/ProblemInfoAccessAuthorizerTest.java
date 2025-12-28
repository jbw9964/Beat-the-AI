package org.app.problem.service;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import org.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Import(ProblemInfoAccessAuthorizerTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ProblemInfoAccessAuthorizerTest extends IntegrationTestSupport {

    @Autowired
    ProblemInfoAccessAuthorizer accessAuthorizer;

    @Autowired
    DataInitFacade data;

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("Public 한 문제거나 내가 만든 문제는 항상 true 를 받는다.")
    void accessable1() {
        Long createdUserId;
        Problem publicProblem, privateProblem;

        {
            createdUserId = data.createUser().getId();
            publicProblem = data.createPublicProblem(createdUserId);
            privateProblem = data.createPrivateProblem(createdUserId);
        }

        assertThat(accessAuthorizer.accessable(
                publicProblem, null
        )).isTrue();
        assertThat(accessAuthorizer.accessable(
                publicProblem, createdUserId
        )).isTrue();
        assertThat(accessAuthorizer.accessable(
                privateProblem, createdUserId
        )).isTrue();
    }

    @Test
    @DisplayName("식별할 수 없는 유저, 탈퇴한 유저는 private 문제에 항상 false 를 받는다.")
    void accessable2() {
        Long withdrawedUserId;
        Problem privateProblem;

        {
            Long anotherUserId = data.createUser().getId();
            privateProblem = data.createPrivateProblem(anotherUserId);

            withdrawedUserId = data.createWithdrawnUser().getId();
        }

        assertThat(accessAuthorizer.accessable(
                privateProblem, null
        )).isFalse();
        assertThat(accessAuthorizer.accessable(
                privateProblem, withdrawedUserId
        )).isFalse();
    }

    @Test
    @DisplayName("Private 문제에 대해 유저가 활성화된 코드를 갖고 있으면 true 를 받는다.")
    void accessable3() {
        Long validCodeHavingUserId;
        Problem privateProblem;

        {
            Long anotherUser = data.createUser().getId();
            privateProblem = data.createPrivateProblem(anotherUser);

            String validCode = "valid code";
            String expiredCode = "expired code";
            Long problemId = privateProblem.getId();

            data.createProblemInvitation(problemId, validCode);

            validCodeHavingUserId = data.createUser().getId();

            data.createRecievedInvitation(
                    validCodeHavingUserId, problemId,
                    validCode, "valid"
            );
            data.createRecievedInvitation(
                    validCodeHavingUserId, problemId,
                    expiredCode, "expired"
            );
        }

        assertThat(accessAuthorizer.accessable(
                privateProblem, validCodeHavingUserId
        )).isTrue();
    }

    @Test
    @DisplayName("Private 문제에 대해 유저가 코드를 갖고 있지 않거나 코드가 활성화되지 않았으면 false 를 받는다.")
    void accessable4() {
        Long nonInvitedUserId;
        Long expiredInvitationHavingUserId;
        Problem privateProblem;

        {
            Long anotherUser = data.createUser().getId();
            privateProblem = data.createPrivateProblem(anotherUser);

            String expiredCode = "expired code";
            Long problemId = privateProblem.getId();

            nonInvitedUserId = data.createUser().getId();
            expiredInvitationHavingUserId = data.createUser().getId();

            data.createRecievedInvitation(
                    expiredInvitationHavingUserId, problemId,
                    expiredCode, "expired"
            );
        }

        assertThat(accessAuthorizer.accessable(
                privateProblem, nonInvitedUserId
        )).isFalse();
        assertThat(accessAuthorizer.accessable(
                privateProblem, expiredInvitationHavingUserId
        )).isFalse();
    }

    @Component
    @Transactional
    protected static class DataInitFacade {

        @Autowired
        GeneralDataInitializer initializer;

        User createUser() {
            return initializer.userBuilder()
                    .name("Test user")
                    .build();
        }

        User createWithdrawnUser() {
            return initializer.userBuilder()
                    .name("Test withdrawn user")
                    .withdrawn(true)
                    .withdrawnAt(LocalDateTime.now())
                    .build();
        }

        Problem createPrivateProblem(Long userId) {
            String title = "Temp title";
            return initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PRIVATE)
                    .serializedScenarioInfo("hi")
                    .build();
        }

        Problem createPublicProblem(Long userId) {
            String title = "Temp title";
            return initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .serializedScenarioInfo("hi")
                    .build();
        }

        void createProblemInvitation(Long problemId, String code) {
            initializer.invitationBuilder()
                    .problemId(problemId)
                    .code(code)
                    .build();
        }

        void createRecievedInvitation(
                Long userId, Long problemId,
                String code, String problemTitle
        ) {
            initializer.receivedInvitationBuilder()
                    .userId(userId)
                    .problemId(problemId)
                    .title(problemTitle)
                    .code(code)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}