package org.app.user.service;

import static org.assertj.core.api.Assertions.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Import(UserInvitationServiceTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class UserInvitationServiceTest extends IntegrationTestSupport {

    static User testUser;

    @Autowired
    UserInvitationService userInvitationService;

    @Autowired
    DataInitFacade data;

    @Autowired
    TestReceivedInvitationRepository receivedInvitationRepo;

    @BeforeEach
    void setUp() {
        testUser = data.createNewUser();
    }

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("사용자는 자신이 수령한 초대코드 목록을 조회할 수 있다.")
    void getMyReceivedInvitations() {
        Long userId = testUser.getId();
        int numOfReceivedInvitation = 10;

        List<ReceivedInvitation> receivedInvitations;

        {
            receivedInvitations = new ArrayList<>(numOfReceivedInvitation);
            for (int i = 0; i < numOfReceivedInvitation; i++) {
                Long problemId = 10L * i;
                String code = String.format("code-%d", i);
                var entity = data.createNewReceivedInvitation(userId, problemId, code);
                receivedInvitations.add(entity);
            }
        }

        int pageNo = 0;
        int pageSize = numOfReceivedInvitation / 2;

        SimplePageResponse<ReceivedInvitationInfo> response
                = userInvitationService.getMyReceivedInvitations(
                userId, pageNo, pageSize
        );

        assertThat(response).isNotNull();
        assertThat(response.pageNoRequest()).isEqualTo(pageNo);
        assertThat(response.pageSizeRequest()).isEqualTo(pageSize);
        assertThat(response.numOfPagedElements()).isEqualTo(pageSize);
        assertThat(response.numOfTotalElements()).isEqualTo(numOfReceivedInvitation);
        assertThat(response.hasNext()).isTrue();

        List<ReceivedInvitationInfo> pagedElements = response.pagedElements();
        assertThat(pagedElements).isNotNull().hasSize(pageSize);

        Map<Long, ReceivedInvitation> entityMap = receivedInvitations.stream()
                .collect(Collectors.toMap(ReceivedInvitation::getId, Function.identity()));

        for (ReceivedInvitationInfo element : pagedElements) {

            assertThat(element).isNotNull();

            Long entityId = element.receivedInvitationId();
            assertThat(entityId).isNotNull();
            assertThat(entityMap).containsKey(entityId);

            ReceivedInvitation entity = entityMap.get(entityId);
            assertThat(entity.getId()).isEqualTo(entityId);
            assertThat(entity.getProblemId()).isEqualTo(element.problemId());
            assertThat(entity.getProblemTitle()).isEqualTo(element.problemTitle());
            assertThat(entity.getCode()).isEqualTo(element.code());
            assertThat(entity.getCreatedAt())
                    .isCloseTo(element.createdAt(), within(Duration.ofSeconds(5L)));

            assertThat(element.isActive()).isFalse();
        }
    }

    @Test
    @DisplayName("사용자는 자신이 수령한 초대코드를 조회할 수 있다.")
    void getMyReceivedInvitation() {
        Long userId = testUser.getId();
        ReceivedInvitation entity = data.createNewReceivedInvitation(
                userId, 100L, "this is test"
        );

        Long receivedInvitationId = entity.getId();
        ReceivedInvitationInfo response = userInvitationService.getMyReceivedInvitation(
                userId, receivedInvitationId
        );

        assertThat(response).isNotNull();

        assertThat(entity.getId()).isEqualTo(response.receivedInvitationId());
        assertThat(entity.getProblemId()).isEqualTo(response.problemId());
        assertThat(entity.getProblemTitle()).isEqualTo(response.problemTitle());
        assertThat(entity.getCode()).isEqualTo(response.code());
        assertThat(entity.getCreatedAt())
                .isCloseTo(response.createdAt(), within(Duration.ofSeconds(5L)));

        assertThat(response.isActive()).isFalse();
    }

    @Test
    @DisplayName("사용자는 초대코드를 수령할 수 있다.")
    void receiveInvitation() {
        Long userId = testUser.getId();
        Long problemId;
        String problemTitle = "this is test";
        String invitationCode = "this is also a test";

        {
            problemId = data.createNewProblem(userId, problemTitle).getId();
            data.createNewInvitation(problemId, invitationCode);
        }

        Long response = userInvitationService.receiveInvitation(userId, invitationCode);

        assertThat(response).isNotNull();

        Optional<ReceivedInvitation> opt = receivedInvitationRepo.findById(response);
        assertThat(opt).isNotEmpty();

        ReceivedInvitation find = opt.get();
        assertThat(find.getId()).isEqualTo(response);
        assertThat(find.getUser().getId()).isEqualTo(userId);
        assertThat(find.getProblemId()).isEqualTo(problemId);
        assertThat(find.getProblemTitle()).isEqualTo(problemTitle);
        assertThat(find.getCode()).isEqualTo(invitationCode);
    }

    @Test
    @DisplayName("사용자는 수령했던 초대코드를 삭제할 수 있다.")
    void deleteMyInvitation() {
        Long userId = testUser.getId();
        Long receivedInvitationId = data.createNewReceivedInvitation(userId, 100L, "this is test")
                .getId();

        Long response = userInvitationService.deleteMyInvitation(userId, receivedInvitationId);

        assertThat(response).isNotNull();

        boolean exists = receivedInvitationRepo.existsById(receivedInvitationId);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("사용자는 초대코드가 유효한지 확인할 수 있다.")
    void testCodeActive() {
        Long userId = testUser.getId();
        int numOfTotal = 10;

        List<ReceivedInvitation> activeRIs;
        List<ReceivedInvitation> inactiveRIs;

        {
            int numOfActive = numOfTotal / 2;
            int numOfInactive = numOfTotal - numOfActive;

            activeRIs = new ArrayList<>(numOfActive);
            inactiveRIs = new ArrayList<>(numOfInactive);

            Problem problem1 = data.createNewProblem(userId);
            Problem problem2 = data.createNewProblem(userId);

            // init for active ones
            for (int i = 0; i < numOfActive; i++) {
                String code = UUID.randomUUID().toString().substring(0, 15);
                Problem problem = i % 2 == 0 ? problem1 : problem2;

                data.createNewInvitation(problem.getId(), code);
                ReceivedInvitation entity = data.createNewReceivedInvitation(userId,
                        problem.getId(), code);
                activeRIs.add(entity);
            }

            // init for inactive ones
            for (int i = 0; i < numOfInactive; i++) {
                String code = UUID.randomUUID().toString().substring(0, 15);
                int mod = i % 3;
                Long problemId = mod == 0 ? i :
                        mod == 1 ? problem1.getId() : problem2.getId();

                ReceivedInvitation entity = data.createNewReceivedInvitation(userId, problemId,
                        code);
                inactiveRIs.add(entity);
            }
        }

        int pageNo = 0;

        List<ReceivedInvitationInfo> infos = userInvitationService.getMyReceivedInvitations(
                userId, pageNo, numOfTotal
        ).pagedElements();

        assertThat(infos).isNotNull().hasSize(numOfTotal);
        assertThat(activeRIs.size() + inactiveRIs.size()).isEqualTo(numOfTotal);

        Map<Long, ReceivedInvitation> activeRIMap = activeRIs.stream()
                .collect(Collectors.toMap(ReceivedInvitation::getId, Function.identity()));
        Map<Long, ReceivedInvitation> inactiveRIMap = inactiveRIs.stream()
                .collect(Collectors.toMap(ReceivedInvitation::getId, Function.identity()));

        for (ReceivedInvitationInfo info : infos) {

            Long receivedInvitationId = info.receivedInvitationId();
            boolean isActive = info.isActive();

            if (activeRIMap.containsKey(receivedInvitationId)) {
                assertThat(isActive).isTrue();
            } else if (inactiveRIMap.containsKey(receivedInvitationId)) {
                assertThat(isActive).isFalse();
            } else {
                throw new AssertionError();
            }
        }

        Long activeId = activeRIs.getFirst().getId();
        Long inactiveId = inactiveRIs.getLast().getId();

        ReceivedInvitationInfo activeResponse = userInvitationService.getMyReceivedInvitation(
                userId,
                activeId);
        ReceivedInvitationInfo inactiveResponse = userInvitationService.getMyReceivedInvitation(
                userId, inactiveId);

        assertThat(activeResponse).isNotNull();
        assertThat(activeResponse.isActive()).isTrue();

        assertThat(inactiveResponse).isNotNull();
        assertThat(inactiveResponse.isActive()).isFalse();
    }

    @Test
    @DisplayName("관련 자원을 찾을 수 없으면 NotFoundException 이 발생한다.")
    void testNotFoundException() {
        Long userId = testUser.getId();
        Long withdrawnUserId = data.createWithdrawnUser().getId();
        Long existingReceivedInvitationId
                = data.createNewReceivedInvitation(userId, 10L, "this is test").getId();
        Long notExistingId = Long.MAX_VALUE;
        String notExistingCode = "notExistingCode";

        // 초대 목록, 내용 보기
        assertThatThrownBy(
                () -> userInvitationService.getMyReceivedInvitations(notExistingId, 0, 10))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(
                () -> userInvitationService.getMyReceivedInvitations(withdrawnUserId, 0, 10))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> userInvitationService.getMyReceivedInvitation(notExistingId,
                existingReceivedInvitationId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> userInvitationService.getMyReceivedInvitation(withdrawnUserId,
                existingReceivedInvitationId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(
                () -> userInvitationService.getMyReceivedInvitation(userId, notExistingId))
                .isInstanceOf(ReceivedInvitationNotFoundException.class);

        // 코드 수령하기
        assertThatThrownBy(
                () -> userInvitationService.receiveInvitation(notExistingId, notExistingCode))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(
                () -> userInvitationService.receiveInvitation(withdrawnUserId, notExistingCode))
                .isInstanceOf(UserNotFoundException.class);

        // 코드 삭제하기
        assertThatThrownBy(() -> userInvitationService.deleteMyInvitation(notExistingId,
                existingReceivedInvitationId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> userInvitationService.deleteMyInvitation(withdrawnUserId,
                existingReceivedInvitationId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> userInvitationService.deleteMyInvitation(userId, notExistingId))
                .isInstanceOf(ReceivedInvitationNotFoundException.class);
    }

    @Test
    @DisplayName("사용자는 오직 자신이 수령한 초대코드만 조회, 삭제할 수 있다.")
    void testForbiddenException() {
        Long userId = testUser.getId();
        Long anotherUserOwnedReceivedInvitationId;

        {
            Long anotherUserId = data.createNewUser().getId();
            anotherUserOwnedReceivedInvitationId = data.createNewReceivedInvitation(anotherUserId,
                            10L, "this is test")
                    .getId();
        }

        assertThatThrownBy(() -> userInvitationService.getMyReceivedInvitation(userId,
                anotherUserOwnedReceivedInvitationId))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> userInvitationService.deleteMyInvitation(userId,
                anotherUserOwnedReceivedInvitationId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("초대코드가 활성화되어있지 않으면 수령할 수 없다.")
    void testInactiveInvitationCodeException() {
        Long userId = testUser.getId();
        String inactiveCode = "not exising";

        assertThatThrownBy(() -> userInvitationService.receiveInvitation(userId, inactiveCode))
                .isInstanceOf(InactiveInvitationCodeException.class);
    }

    @Test
    @DisplayName("동일한 초대코드는 중복으로 수령할 수 없다.")
    void testAlreadyReceivedInvitationException() {
        Long userId = testUser.getId();
        String alreadyReceivedInvitationCode = "alreadyReceivedInvitationCode";

        {
            Long problemId = data.createNewProblem(userId).getId();
            data.createNewInvitation(problemId, alreadyReceivedInvitationCode);
            data.createNewReceivedInvitation(userId, problemId, alreadyReceivedInvitationCode);
        }

        assertThatThrownBy(() -> userInvitationService.receiveInvitation(userId,
                alreadyReceivedInvitationCode))
                .isInstanceOf(AlreadyReceivedInvitationException.class);
    }


    @Component
    @Transactional
    @SuppressWarnings("UnusedReturnValue")
    protected static class DataInitFacade {

        @Autowired
        GeneralDataInitializer dataInitializer;

        User createNewUser() {
            return dataInitializer.createUser("test", null, null, null, null, false, null);
        }

        User createWithdrawnUser() {
            return dataInitializer.createUser("test", null, null, null, null, true, null);
        }

        ReceivedInvitation createNewReceivedInvitation(Long userId, Long problemId, String code) {
            return dataInitializer.createReceivedInvitation(userId, problemId, code, code);
        }

        Problem createNewProblem(Long userId) {
            return dataInitializer.createProblem(
                    userId, "temp", null, null, 3, 5,
                    ProblemVisibility.PRIVATE, "temp"
            );
        }

        Problem createNewProblem(Long userId, String title) {
            return dataInitializer.createProblem(
                    userId, title, null, null, 3, 5,
                    ProblemVisibility.PRIVATE, "temp"
            );
        }

        Invitation createNewInvitation(Long problemId, String code) {
            return dataInitializer.createInvitation(problemId, code);
        }

        void initAll() {
            dataInitializer.initAll();
        }
    }
}