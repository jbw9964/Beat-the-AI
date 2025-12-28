package org.app.problem.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import org.*;
import org.TestUtils.*;
import org.app.entity.*;
import org.app.problem.domain.exception.*;
import org.app.problem.dto.*;
import org.app.problem.dto.response.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.transaction.annotation.*;
import org.support.*;


@Import(ProblemRewardServiceTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ProblemRewardServiceTest extends IntegrationTestSupport {

    @Autowired
    ProblemRewardService service;

    @MockitoSpyBean
    ProblemInfoAccessAuthorizer accessAuthorizer;

    @Autowired
    DataInitFacade data;

    @Autowired
    TestProblemRewardRepository rewardRepo;

    @Autowired
    TestProblemRepository problemRepo;

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("문제 보상 목록을 조회할 수 있다.")
    void getRewards() {
        Long publidProblemId;
        Long privateProblemId;
        Long createdUserId;

        int numOfTotal = 10;
        List<ProblemReward> publicProblemRewards;
        List<ProblemReward> privateProblemRewards;

        {
            createdUserId = data.createUser().getId();
            publidProblemId = data.createPublicProblem(
                    createdUserId, "title"
            ).getId();
            privateProblemId = data.createPrivateProblem(
                    createdUserId, "title"
            ).getId();

            publicProblemRewards = new ArrayList<>(numOfTotal);
            privateProblemRewards = new ArrayList<>(numOfTotal);

            for (int i = 0; i < numOfTotal; i++) {
                Long aId = data.createActualRewardImage().getId();
                Long oId = data.createOverviewRewardImage().getId();
                String desc = String.format("Desc - %d", i);

                publicProblemRewards.add(data.createProblemReward(
                        publidProblemId, aId, oId, desc
                ));
            }

            for (int i = 0; i < numOfTotal; i++) {
                Long aId = data.createActualRewardImage().getId();
                Long oId = data.createOverviewRewardImage().getId();
                String desc = String.format("Private desc - %d", i);

                privateProblemRewards.add(data.createProblemReward(
                        privateProblemId, aId, oId, desc
                ));
            }
        }

        int pageNo = 0;

        for (int i = 0; i < 2; i++) {

            Long userId = (i & 0b01) == 0b1 ? null : createdUserId;
            GetProblemRewardsResponse response = service.getRewards(
                    publidProblemId, userId, pageNo, numOfTotal
            );

            assertThat(response).isNotNull();
            assertThat(response.isMine()).isEqualTo(
                    createdUserId.equals(userId)
            );

            SimplePageResponse<ProblemRewardInfo> pagedResponse
                    = response.pageResponse();
            TestUtils.assertSimplePageResponseEquality(
                    pagedResponse, pageNo, numOfTotal,
                    numOfTotal, numOfTotal, false
            );

            List<ProblemRewardInfo> elements = pagedResponse.pagedElements();
            assertThat(elements).isNotNull().hasSize(numOfTotal);

            Map<Long, ProblemReward> publicRewardMap = publicProblemRewards.stream()
                    .collect(Collectors.toMap(ProblemReward::getId, Function.identity()));

            for (ProblemRewardInfo element : elements) {

                assertThat(element).isNotNull();

                Long entityId = element.problemRewardId();
                assertThat(entityId).isNotNull();
                assertThat(publicRewardMap).containsKey(entityId);

                ProblemReward entity = publicRewardMap.get(entityId);
                assertThat(entity.getId()).isEqualTo(entityId);
                assertThat(entity.getDescription()).isEqualTo(element.description());
                assertThat(entity.hasTransferred()).isEqualTo(element.hasTransferred());
                assertThat(entity.getCreatedAt())
                        .isCloseTo(element.createdAt(), within(Duration.ofSeconds(5L)));
            }
        }

        GetProblemRewardsResponse response = service.getRewards(
                privateProblemId, createdUserId, pageNo, numOfTotal
        );

        assertThat(response).isNotNull();
        assertThat(response.isMine()).isTrue();

        SimplePageResponse<ProblemRewardInfo> pagedResponse
                = response.pageResponse();
        TestUtils.assertSimplePageResponseEquality(
                pagedResponse, pageNo, numOfTotal,
                numOfTotal, numOfTotal, false
        );

        List<ProblemRewardInfo> elements = pagedResponse.pagedElements();
        assertThat(elements).isNotNull().hasSize(numOfTotal);

        Map<Long, ProblemReward> privateRewardMap = privateProblemRewards.stream()
                .collect(Collectors.toMap(ProblemReward::getId, Function.identity()));

        for (ProblemRewardInfo element : elements) {

            assertThat(element).isNotNull();

            Long entityId = element.problemRewardId();
            assertThat(entityId).isNotNull();
            assertThat(privateRewardMap).containsKey(entityId);

            ProblemReward entity = privateRewardMap.get(entityId);
            assertThat(entity.getId()).isEqualTo(entityId);
            assertThat(entity.getDescription()).isEqualTo(element.description());
            assertThat(entity.hasTransferred()).isEqualTo(element.hasTransferred());
            assertThat(entity.getCreatedAt())
                    .isCloseTo(element.createdAt(), within(Duration.ofSeconds(5L)));
        }
    }

    @Test
    @DisplayName("문제 보상 미리보기 이미지 id 를 식별할 수 있다.")
    void getOverviewRewardImageId() {
        Long publicProblemId, publicProblemRewardId, publicOverviewId;
        Long privateProblemId, privateProblemRewardId, privateOverviewId;
        Long createdUserId;

        {
            createdUserId = data.createUser().getId();

            publicProblemId = data.createPublicProblem(
                    createdUserId, "title"
            ).getId();
            privateProblemId = data.createPrivateProblem(
                    createdUserId, "title"
            ).getId();

            Long aId1 = data.createActualRewardImage().getId();
            publicOverviewId = data.createOverviewRewardImage().getId();
            publicProblemRewardId = data.createProblemReward(
                    publicProblemId, aId1, publicOverviewId, "desc"
            ).getId();

            Long aId2 = data.createActualRewardImage().getId();
            privateOverviewId = data.createOverviewRewardImage().getId();
            privateProblemRewardId = data.createProblemReward(
                    privateProblemId, aId2, privateOverviewId, "desc"
            ).getId();
        }

        for (int i = 0; i < 2; i++) {
            Long userId = (i & 0b01) == 0b01 ?
                    null : createdUserId;

            Long response = service.getOverviewRewardImageId(
                    publicProblemId, publicProblemRewardId, userId
            );

            assertThat(response).isNotNull().isEqualTo(publicOverviewId);
        }

        Long response = service.getOverviewRewardImageId(
                privateProblemId, privateProblemRewardId, createdUserId
        );

        assertThat(response).isNotNull().isEqualTo(privateOverviewId);
    }

    @Test
    @DisplayName("문제 작성자는 실제 보상 이미지 id 를 식별할 수 있다.")
    void getActualRewardImageId() {
        Long publicProblemId, publicProblemRewardId, publicActualId;
        Long privateProblemId, privateProblemRewardId, privateActualId;
        Long createdUserId;

        {
            createdUserId = data.createUser().getId();

            publicProblemId = data.createPublicProblem(
                    createdUserId, "title"
            ).getId();
            privateProblemId = data.createPrivateProblem(
                    createdUserId, "title"
            ).getId();

            publicActualId = data.createActualRewardImage().getId();
            Long oid1 = data.createOverviewRewardImage().getId();
            publicProblemRewardId = data.createProblemReward(
                    publicProblemId, publicActualId, oid1, "desc"
            ).getId();

            privateActualId = data.createActualRewardImage().getId();
            Long oid2 = data.createOverviewRewardImage().getId();
            privateProblemRewardId = data.createProblemReward(
                    privateProblemId, privateActualId, oid2, "desc"
            ).getId();
        }

        for (int i = 0; i < 2; i++) {

            boolean publicCase = (i & 0b01) == 0b01;

            Long pid = publicCase ? publicProblemId : privateProblemId;
            Long rid = publicCase ? publicProblemRewardId : privateProblemRewardId;
            Long expected = publicCase ? publicActualId : privateActualId;

            Long response = service.getActualRewardImageId(pid, rid, createdUserId);
            assertThat(response).isNotNull().isEqualTo(expected);
        }
    }

    @Test
    @DisplayName("보상 생선 전 문제 또는 사용자를 식별할 수 없으면 NotFoundException 을, "
                 + "문제 작성자가 아니면 ForbiddenException 이 발생한다.")
    void validateBeforeCreateReward() {
        Long notExsitingProblemId, softDeletedProblemId;
        //noinspection WrapperTypeMayBePrimitive
        Long notExsitingUserId, withdrawnUserId;

        Long existingProblemId, createdUserId, anotherUserId;

        {
            notExsitingProblemId = notExsitingUserId = Long.MAX_VALUE;

            createdUserId = data.createUser().getId();
            existingProblemId = data.createPublicProblem(
                    createdUserId, "title"
            ).getId();
            anotherUserId = data.createUser().getId();

            softDeletedProblemId = data.createSoftDeletedProblem(
                    createdUserId, "title"
            ).getId();
            withdrawnUserId = data.createWithdrawnUser().getId();
        }

        Class<ProblemNotFoundException> problemNotFoundEx = ProblemNotFoundException.class;
        Class<UserNotFoundException> userNotFoundEx = UserNotFoundException.class;
        Class<ForbiddenException> forbiddenEx = ForbiddenException.class;

        BiConsumer<Long, Long> func = (pid, uid) ->
                service.validateBeforeCreateReward(pid, uid);

        TestUtils.assertThrow(      // 문제 없을 때
                notExsitingProblemId, createdUserId,
                func, problemNotFoundEx
        );
        TestUtils.assertThrow(      // 문제 삭제 예정일 때
                softDeletedProblemId, createdUserId,
                func, problemNotFoundEx
        );

        TestUtils.assertThrow(      // 사용자 없을 때
                existingProblemId, notExsitingUserId,
                func, userNotFoundEx
        );
        TestUtils.assertThrow(      // 탈퇴한 사용자일 때
                existingProblemId, withdrawnUserId,
                func, userNotFoundEx
        );

        TestUtils.assertThrow(      // 문제 작성자 외 다른 사람이 시도할 때
                existingProblemId, anotherUserId,
                func, forbiddenEx
        );
    }

    @Test
    @DisplayName("문제 보상 정보를 생성할 수 있다.")
    void createReward() {
        Long problemId;
        Long actualRewardImageId, overviewRewardImageId;
        String description = "This is description";

        {
            Long userId = data.createUser().getId();
            problemId = data.createPrivateProblem(
                    userId, "title"
            ).getId();

            actualRewardImageId = data.createActualRewardImage().getId();
            overviewRewardImageId = data.createOverviewRewardImage().getId();
        }

        Long response = service.createReward(
                problemId, actualRewardImageId,
                overviewRewardImageId, description
        );

        assertThat(response).isNotNull();

        Optional<ProblemReward> opt = rewardRepo.findById(response);
        assertThat(opt).isPresent();

        ProblemReward find = opt.get();
        assertThat(find.getId()).isEqualTo(response);
        assertThat(find.getDescription()).isEqualTo(description);
        assertThat(find.getActualRewardImage().getId())
                .isEqualTo(actualRewardImageId);
        assertThat(find.getOverviewRewardImage().getId())
                .isEqualTo(overviewRewardImageId);
        assertThat(find.hasTransferred()).isFalse();
        assertThat(find.getCreatedAt()).isNotNull();

        Problem problem = problemRepo.findById(problemId)
                .orElseThrow(AssertionError::new);

        assertThat(problem.getNumOfRewardSets()).isOne();
    }

    @Test
    @DisplayName("문제 작성자는 보상 상세 설명을 바꿀 수 있다.")
    void updateRewardDescription() {
        Long problemId, problemRewardId;
        Long createdUserId;
        String changedDescription = "I'm changed";

        {
            createdUserId = data.createUser().getId();
            problemId = data.createPrivateProblem(
                    createdUserId, "title"
            ).getId();

            Long aid = data.createActualRewardImage().getId();
            Long oid = data.createOverviewRewardImage().getId();

            problemRewardId = data.createProblemReward(
                    problemId, aid, oid, "origin"
            ).getId();
        }

        Long response = service.updateRewardDescription(
                problemId, problemRewardId, createdUserId, changedDescription
        );

        assertThat(response).isNotNull().isEqualTo(problemRewardId);

        ProblemReward find = rewardRepo.findById(response)
                .orElseThrow(AssertionError::new);

        assertThat(find.getDescription()).isEqualTo(changedDescription);
    }

    @Test
    @DisplayName("문제 삭제 전 문제, 보상, 사용자를 식별할 수 없으면 NotFoundException 을, "
                 + "문제 작성자가 아니면 ForbiddenException 이 발생한다.")
    void validateBeforeDeleteReward() {
        Long notExsitingProblemId, softDeletedProblemId;
        Long notExisintgRewardId;
        //noinspection WrapperTypeMayBePrimitive
        Long notExsitingUserId, withdrawnUserId;

        Long existingProblemId, exsitingRewardId;
        Long createdUserId, anotherUserId;

        {
            notExsitingProblemId = notExisintgRewardId = notExsitingUserId
                    = Long.MAX_VALUE;

            createdUserId = data.createUser().getId();
            anotherUserId = data.createUser().getId();
            withdrawnUserId = data.createWithdrawnUser().getId();

            existingProblemId = data.createPrivateProblem(
                    createdUserId, "title"
            ).getId();

            Long aid = data.createActualRewardImage().getId();
            Long oid = data.createOverviewRewardImage().getId();
            exsitingRewardId = data.createProblemReward(
                    existingProblemId, aid, oid, "reward"
            ).getId();

            softDeletedProblemId = data.createSoftDeletedProblem(
                    createdUserId, "title"
            ).getId();
        }

        Triplet<Long, Long, Long, ?> getOverviewIdFunc = (pid, rid, uid)
                -> service.getRewardImageIdsInfoBeforeRemoval(pid, rid, uid);

        TestUtils.assertThrow(      // 문제 없을 때
                notExsitingProblemId, exsitingRewardId, createdUserId,
                getOverviewIdFunc, ProblemNotFoundException.class
        );
        TestUtils.assertThrow(      // 문제 삭제 예정일 때
                softDeletedProblemId, exsitingRewardId, createdUserId,
                getOverviewIdFunc, ProblemNotFoundException.class
        );

        TestUtils.assertThrow(      // 보상 없을 때
                existingProblemId, notExisintgRewardId, createdUserId,
                getOverviewIdFunc, ProblemRewardNotFoundException.class
        );

        TestUtils.assertThrow(      // 사용자 없을 때
                existingProblemId, exsitingRewardId, notExsitingUserId,
                getOverviewIdFunc, UserNotFoundException.class
        );
        TestUtils.assertThrow(      // 탈퇴한 사용자일 때
                existingProblemId, exsitingRewardId, withdrawnUserId,
                getOverviewIdFunc, UserNotFoundException.class
        );

        TestUtils.assertThrow(      // 문제 작성자 외 다른 사람이 시도할 때
                existingProblemId, exsitingRewardId, anotherUserId,
                getOverviewIdFunc, ForbiddenException.class
        );
    }

    @Test
    @DisplayName("문제 작성자는 연관된 보상을 삭제할 수 있다.")
    void deleteReward() {
        Long problemId, problemRewardId;

        {
            Long createdUserId = data.createUser().getId();
            problemId = data.createPrivateProblem(
                    createdUserId, "title"
            ).getId();

            Long aid = data.createActualRewardImage().getId();
            Long oid = data.createOverviewRewardImage().getId();
            problemRewardId = data.createProblemReward(
                    problemId, aid, oid, "desc"
            ).getId();
        }

        Long response = service.deleteReward(problemId, problemRewardId);

        assertThat(response).isNotNull().isEqualTo(problemRewardId);

        Optional<ProblemReward> find = rewardRepo.findById(response);
        assertThat(find).isEmpty();
    }

    @Test
    @DisplayName("관련 자원을 찾을 수 없으면 NotFoundException 이 발생한다.")
    void testNotFoundException() {
        Long notExsitingProblemId, softDeletedProblemId;
        Long notExisintgRewardId;
        Long notExsitingUserId, withdrawnUserId;

        Long existingPublicProblemId, exsitingRewardId, createdUserId;

        {
            notExsitingProblemId = notExisintgRewardId = notExsitingUserId
                    = Long.MAX_VALUE;

            createdUserId = data.createUser().getId();
            withdrawnUserId = data.createWithdrawnUser().getId();

            existingPublicProblemId = data.createPublicProblem(
                    createdUserId, "title"
            ).getId();
            softDeletedProblemId = data.createSoftDeletedProblem(
                    createdUserId, "ttile"
            ).getId();

            Long aid = data.createActualRewardImage().getId();
            Long oid = data.createOverviewRewardImage().getId();
            exsitingRewardId = data.createProblemReward(
                    existingPublicProblemId, aid, oid, "desc"
            ).getId();
        }

        // 문제 보상 목록 볼 때
        {
            int pageNo = 0, pageSize = 5;

            Function<Long, ?> func = (pid) -> service.getRewards(
                    pid, withdrawnUserId, pageNo, pageSize
            );

            TestUtils.assertThrow(      // 문제 없을 때
                    notExsitingProblemId,
                    func, ProblemNotFoundException.class
            );
            TestUtils.assertThrow(      // 문제 삭제 예정일 때
                    softDeletedProblemId,
                    func, ProblemNotFoundException.class
            );
        }

        // 문제 보상 미리보기 id 조회할 때
        {
            BiFunction<Long, Long, ?> func = (pid, rid)
                    -> service.getOverviewRewardImageId(pid, rid, notExsitingUserId);

            TestUtils.assertThrow(      // 문제 없을 때
                    notExsitingProblemId, exsitingRewardId,
                    func, ProblemNotFoundException.class
            );
            TestUtils.assertThrow(      // 문제 삭제 예정일 때
                    softDeletedProblemId, exsitingRewardId,
                    func, ProblemNotFoundException.class
            );

            TestUtils.assertThrow(      // 보상 없을 때
                    existingPublicProblemId, notExisintgRewardId,
                    func, ProblemRewardNotFoundException.class
            );
        }

        // 문제 실제 보상 이미지 볼 때
        {
            BiFunction<Long, Long, ?> func = (pid, rid)
                    -> service.getActualRewardImageId(pid, rid, createdUserId);

            TestUtils.assertThrow(      // 문제 없을 때
                    notExsitingProblemId, exsitingRewardId,
                    func, ProblemNotFoundException.class
            );
            TestUtils.assertThrow(      // 문제 삭제 예정일 때
                    softDeletedProblemId, exsitingRewardId,
                    func, ProblemNotFoundException.class
            );

            TestUtils.assertThrow(      // 보상 없을 때
                    existingPublicProblemId, notExisintgRewardId,
                    func, ProblemRewardNotFoundException.class
            );
        }

        // 문제 보상 설명 수정할 때
        {
            TestUtils.Triplet<Long, Long, Long, ?> func = (pid, rid, uid)
                    -> service.updateRewardDescription(pid, rid, uid, "desc");

            TestUtils.assertThrow(      // 문제 없을 때
                    notExsitingProblemId, exsitingRewardId, createdUserId,
                    func, ProblemNotFoundException.class
            );
            TestUtils.assertThrow(      // 문제 삭제 예정일 때
                    softDeletedProblemId, exsitingRewardId, createdUserId,
                    func, ProblemNotFoundException.class
            );

            TestUtils.assertThrow(      // 보상 없을 때
                    existingPublicProblemId, notExisintgRewardId, createdUserId,
                    func, ProblemRewardNotFoundException.class
            );

            TestUtils.assertThrow(      // 사용자 없을 때
                    existingPublicProblemId, exsitingRewardId, notExsitingUserId,
                    func, UserNotFoundException.class
            );
            TestUtils.assertThrow(      // 탈퇴한 사용자일 때
                    existingPublicProblemId, exsitingRewardId, withdrawnUserId,
                    func, UserNotFoundException.class
            );
        }

        // 문제 보상 추가할 때 case 는 이전 `validateBeforeCreateReward` 에서 검증함.
        // 문제 보상 삭제할 때 case 는 이전 `validateBeforeDeleteReward` 에서 검증함.
    }

    @Test
    @DisplayName("보상 추가, 삭제 시 관련 entity 를 찾지 못하면 "
                 + "UnexpectedNotFoundException 이 발생한다.")
    void testUnexpectedNotFoundException() {
        Long notExsitingProblemId, softDeletedProblemId;
        //noinspection WrapperTypeMayBePrimitive
        Long notExsitingProblemRewardId;
        Long notExsitingActualRewardImageId;
        //noinspection WrapperTypeMayBePrimitive
        Long notExsitingOverviewRewardImageId;

        Long exsitingProblemId, exsitingProblemRewardId;
        Long exsitingActualRewardImageId;
        Long exsitingOverviewRewardImageId;

        {
            notExsitingProblemId = notExsitingProblemRewardId = Long.MAX_VALUE;
            notExsitingActualRewardImageId = notExsitingOverviewRewardImageId
                    = Long.MAX_VALUE;

            Long userId = data.createUser().getId();
            softDeletedProblemId = data.createSoftDeletedProblem(
                    userId, "ttt"
            ).getId();

            exsitingProblemId = data.createPublicProblem(
                    userId, "tttt"
            ).getId();
            exsitingActualRewardImageId = data.createActualRewardImage().getId();
            exsitingOverviewRewardImageId = data.createOverviewRewardImage().getId();
            exsitingProblemRewardId = data.createProblemReward(
                    exsitingProblemId, exsitingActualRewardImageId,
                    exsitingOverviewRewardImageId, "desc"
            ).getId();
        }

        // 문제 보상 추가할 때
        {
            TestUtils.Triplet<Long, Long, Long, ?> func = (pid, aid, oid)
                    -> service.createReward(pid, aid, oid, "desc");

            TestUtils.assertThrow(      // 문제 없을 때
                    notExsitingProblemId,
                    exsitingActualRewardImageId, exsitingOverviewRewardImageId,
                    func, UnexpectedNotFoundException.class
            );
            TestUtils.assertThrow(      // 문제 삭제 예정일 때
                    softDeletedProblemId,
                    exsitingActualRewardImageId, exsitingOverviewRewardImageId,
                    func, UnexpectedNotFoundException.class
            );

            TestUtils.assertThrow(      // actual reward image 없을 때
                    exsitingProblemId,
                    notExsitingActualRewardImageId, exsitingOverviewRewardImageId,
                    func, UnexpectedNotFoundException.class
            );
            TestUtils.assertThrow(      // overview reward image 없을 때
                    exsitingProblemId,
                    exsitingActualRewardImageId, notExsitingOverviewRewardImageId,
                    func, UnexpectedNotFoundException.class
            );

        }

        // 문제 보상 삭제할 때
        {
            BiFunction<Long, Long, ?> func = (pid, rid)
                    -> service.deleteReward(pid, rid);

            TestUtils.assertThrow(      // 문제 없을 때
                    notExsitingProblemId, exsitingProblemRewardId,
                    func, UnexpectedNotFoundException.class
            );
            TestUtils.assertThrow(      // 문제 삭제 예정일 때
                    softDeletedProblemId, exsitingProblemRewardId,
                    func, UnexpectedNotFoundException.class
            );

            TestUtils.assertThrow(      // 보상 없을 때
                    exsitingProblemId, notExsitingProblemRewardId,
                    func, UnexpectedNotFoundException.class
            );
        }
    }

    @Test
    @DisplayName("권한 없는 사용자가 자원을 조회, 생성, 수정 및 삭제를 시도하면 "
                 + "ForbiddenException 이 발생한다.")
    void testForbiddenException() {

        Long privateProblemId, problemRewardId;
        Long uninvitedUserId = Long.MAX_VALUE / 2,
                notExsitingUserId = Long.MAX_VALUE / 3;
        Long anotherUserId;

        {
            Long createdUserId = data.createUser().getId();
            anotherUserId = data.createUser().getId();

            privateProblemId = data.createPrivateProblem(
                    createdUserId, "ttt"
            ).getId();

            Long aid = data.createActualRewardImage().getId();
            Long oid = data.createOverviewRewardImage().getId();
            problemRewardId = data.createProblemReward(
                    privateProblemId, aid, oid, "desc"
            ).getId();

            doAnswer(invocation -> true)
                    .when(accessAuthorizer)
                    .accessable(any(), eq(createdUserId));
            doAnswer(invocation -> false)
                    .when(accessAuthorizer)
                    .accessable(
                            any(),
                            AdditionalMatchers.not(eq(createdUserId))
                    );
        }

        Class<ForbiddenException> forbiddenEx = ForbiddenException.class;

        // Private 문제의 보상 목록을 조회할 때
        {
            int pageNo = 0, pageSize = 20;

            Function<Long, ?> func = (uid) -> service.getRewards(
                    privateProblemId, uid, pageNo, pageSize
            );

            TestUtils.assertThrow(      // 없는 사용자 id
                    notExsitingUserId, func, forbiddenEx
            );
            TestUtils.assertThrow(      // 초대받지 않은 사용자 id
                    uninvitedUserId, func, forbiddenEx
            );
        }

        // Private 문제의 보상 미리보기 이미지 id 를 조회할 때
        {
            Function<Long, ?> func = (uid) -> service.getOverviewRewardImageId(
                    privateProblemId, problemRewardId, uid
            );

            TestUtils.assertThrow(      // 없는 사용자 id
                    notExsitingUserId, func, forbiddenEx
            );
            TestUtils.assertThrow(      // 초대받지 않은 사용자 id
                    uninvitedUserId, func, forbiddenEx
            );
        }

        // 문제의 실제 보상 이미지 id 를 조회할 때
        assertThatThrownBy(() -> service.getActualRewardImageId(
                privateProblemId, problemRewardId, anotherUserId
        ))
                .isInstanceOf(forbiddenEx);

        // 문제 보상 설명을 수정할 때
        assertThatThrownBy(() -> service.updateRewardDescription(
                privateProblemId, problemRewardId, anotherUserId, "descc"
        ))
                .isInstanceOf(forbiddenEx);

        // 문제 보상 추가할 때 case 는 이전 `validateBeforeCreateReward` 에서 검증함.
        // 문제 보상 삭제할 때 case 는 이전 `validateBeforeDeleteReward` 에서 검증함.
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

        Problem createPublicProblem(
                Long userId, String title
        ) {
            return initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .serializedScenarioInfo("hi")
                    .build();
        }

        Problem createPrivateProblem(
                Long userId, String title
        ) {
            return initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PRIVATE)
                    .serializedScenarioInfo("hi")
                    .build();
        }

        Problem createSoftDeletedProblem(
                Long userId, String title
        ) {
            Problem problem = initializer.problemBuilder()
                    .userId(userId)
                    .title(title)
                    .visibility(ProblemVisibility.PUBLIC)
                    .serializedScenarioInfo("hi")
                    .build();

            problem.reserveRemoval(
                    LocalDateTime.now(),
                    LocalDate.now()
            );

            return problem;
        }

        ActualRewardImage createActualRewardImage() {
            String path = UUID.randomUUID().toString();
            return initializer.actualRewardImageBuilder()
                    .storageType(RewardStorageType.SERVER)
                    .actualImagePath(path)
                    .build();
        }

        OverviewRewardImage createOverviewRewardImage() {
            String path = UUID.randomUUID().toString();
            return initializer.overviewRewardImageBuilder()
                    .storageType(RewardStorageType.SERVER)
                    .overviewImagePath(path)
                    .build();
        }

        ProblemReward createProblemReward(
                Long problemId, Long actualRewardImageId,
                Long overviewRewardImageId, String description
        ) {
            return initializer.problemRewardBuilder()
                    .problemId(problemId)
                    .actualRewardImageId(actualRewardImageId)
                    .overviewRewardImageId(overviewRewardImageId)
                    .description(description)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}