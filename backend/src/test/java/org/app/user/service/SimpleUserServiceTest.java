package org.app.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.*;
import org.*;
import org.app.config.domain.*;
import org.app.entity.*;
import org.app.user.domain.exception.*;
import org.app.user.dto.*;
import org.app.user.event.*;
import org.app.user.repository.*;
import org.app.user.service.SimpleUserServiceTest.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.event.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@RecordApplicationEvents
@Import(DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class SimpleUserServiceTest extends IntegrationTestSupport {

    static final String testPassword = "testPASSWORD";
    static final ScenarioInfo scenarioInfoSample
            = new ScenarioInfo(0, "hi", "goodbye");
    static User testUser;
    @Autowired
    SimpleUserService simpleUserService;

    @Autowired
    ApplicationEvents applicationEvents;

    @Autowired
    DataInitFacade data;

    @Autowired
    UserRepository userRepo;

    @Autowired
    PasswordEncoder pwEncoder;

    @BeforeEach
    void setUp() {
        applicationEvents.clear();
        testUser = data.createNewUser(
                "test", "testEMAIL", "testTHUMBNAIL", testPassword
        );
    }

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("사용자는 탈퇴할 수 있다.")
    void withdrawMe() {
        Long userId = testUser.getId();

        Long response = simpleUserService.withdrawMe(userId);

        assertThat(response).isNotNull().isEqualTo(userId);

        Optional<User> find = userRepo.findById(userId);
        assertThat(find).isNotEmpty();

        User get = find.get();
        assertThat(get.withdrawn()).isTrue();
    }

    @Test
    @DisplayName("사용자 탈퇴시 이벤트가 발행된다.")
    void testWithdrawEvent() {
        Long userId = testUser.getId();
        long timeToAwaitEventPub = 5L;

        simpleUserService.withdrawMe(userId);

        await()
                .atMost(Duration.ofSeconds(timeToAwaitEventPub))
                .pollDelay(Duration.ofMillis(50L))
                .untilAsserted(() -> {

                    List<UserWithdrawEvent> withdrawEvents
                            = applicationEvents.stream(UserWithdrawEvent.class).toList();

                    assertThat(withdrawEvents).hasSize(1);

                    UserWithdrawEvent event = withdrawEvents.getFirst();
                    assertThat(event).isNotNull();
                    assertThat(event.userId()).isEqualTo(userId);
                });
    }

    @Test
    @DisplayName("사용자 정보를 수정할 수 있다.")
    void updateMyInfo() {
        Long userId = testUser.getId();
        String newName = "NEW NAME";
        String newEmail = "NEW EMAIL";
        String newThumbnail = "NEW THUMBNAIL";

        Long response = simpleUserService.updateMyInfo(userId, newName, newEmail, newThumbnail);

        assertThat(response).isNotNull().isEqualTo(userId);

        Optional<User> find = userRepo.findById(userId);
        assertThat(find).isNotEmpty();

        User get = find.get();
        assertThat(get.getName()).isEqualTo(newName);
        assertThat(get.getEmail()).isEqualTo(newEmail);
        assertThat(get.getThumbnailUrl()).isEqualTo(newThumbnail);
    }

    @Test
    @DisplayName("아직 사용자 설정은 변경할 수 없다.")
    void updateMySetting() {
        // TODO : 사용자 설정 변경 구현 후 테스트 구성하기

        Long userId = testUser.getId();

        assertThatThrownBy(() -> simpleUserService.updateMySetting(userId))
                .isInstanceOf(NotImplementedException.class);
    }

    @Test
    @DisplayName("사용자 비밀번호를 변경할 수 있다.")
    void updateMyPassword() {
        Long userId = testUser.getId();
        String newPassword = "NEW PASSWORD";

        Long response = simpleUserService.updateMyPassword(userId, testPassword, newPassword);

        assertThat(response).isNotNull().isEqualTo(userId);

        Optional<User> find = userRepo.findById(userId);
        assertThat(find).isNotEmpty();

        String encryptedPassword = find.get().getEncryptedPassword();
        assertThat(encryptedPassword).isNotEmpty();
        assertThat(pwEncoder.matches(newPassword, encryptedPassword)).isTrue();
    }

    @Test
    @DisplayName("스스로 생성한 문제 목록을 조회할 수 있다.")
    void getMyProblems() {
        Long userId = testUser.getId();

        int numOfProblems = 10;
        List<Problem> problems;

        {
            problems = new ArrayList<>(numOfProblems);
            ScenarioInfo[] scenarioInfo = new ScenarioInfo[]{scenarioInfoSample};
            for (int i = 0; i < numOfProblems; i++) {
                int mod = i % 2;
                String title = String.format("title-%d", i);
                int nOfSToGetReward = i + 10;
                int nOfSToFailPlay = nOfSToGetReward / 2;
                ProblemVisibility visibility =
                        mod == 0 ? ProblemVisibility.PRIVATE : ProblemVisibility.PUBLIC;

                Problem problem = data.createNewProblem(
                        userId, title, nOfSToGetReward, nOfSToFailPlay,
                        visibility, scenarioInfo
                );

                problems.add(problem);
            }
        }

        int pageNo = 0;
        int pageSize = problems.size() / 2;

        SimplePageResponse<SimpleProblemInfo> response = simpleUserService.getMyProblems(
                userId, pageNo, pageSize
        );

        assertThat(response).isNotNull();
        assertThat(response.pageNoRequest()).isEqualTo(pageNo);
        assertThat(response.pageSizeRequest()).isEqualTo(pageSize);
        assertThat(response.numOfPagedElements()).isEqualTo(pageSize);
        assertThat(response.numOfTotalElements()).isEqualTo(problems.size());
        assertThat(response.hasNext()).isTrue();

        List<SimpleProblemInfo> pagedElements = response.pagedElements();
        assertThat(pagedElements).isNotNull().hasSize(pageSize);

        Map<Long, Problem> problemMap = problems.stream()
                .collect(Collectors.toMap(Problem::getId, Function.identity()));

        for (SimpleProblemInfo element : pagedElements) {

            assertThat(element).isNotNull();

            Long problemId = element.problemId();
            assertThat(problemMap).containsKey(problemId);

            Problem problem = problemMap.get(problemId);
            assertThat(problem.getId()).isEqualTo(element.problemId());
            assertThat(problem.getTitle()).isEqualTo(element.title());
            assertThat(problem.getDescription()).isEqualTo(element.description());
            assertThat(problem.getRewardMessage()).isEqualTo(element.rewardMessage());
            assertThat(problem.getNumOfScenariosToGetReward())
                    .isEqualTo(element.numOfScenariosToGetReward());
            assertThat(problem.getNumOfScenariosToFailPlay())
                    .isEqualTo(element.numOfScenariosToFailPlay());
            assertThat(problem.getVisibility()).isEqualTo(element.visibility());
            assertThat(problem.getCreatedAt())
                    .isCloseTo(element.createdAt(), within(Duration.ofSeconds(5L)));
            assertThat(problem.getModifiedAt()).isNull();
        }
    }

    @Test
    @DisplayName("스스로 생성한 문제 세부 내용을 조회할 수 있다.")
    void getMyProblem() {
        Long userId = testUser.getId();

        int numOfScenarios = 10;
        ScenarioInfo[] scenarioInfos;
        Problem problem;
        Long problemId;

        {
            scenarioInfos = new ScenarioInfo[numOfScenarios];
            for (int i = 0; i < numOfScenarios; i++) {
                String sContent = String.format("SCENARIO-%d", i);
                String aContent = String.format("ANSWER-%d", i);
                scenarioInfos[i] = new ScenarioInfo(i, sContent, aContent);
            }

            problem = data.createNewProblem(
                    userId, "TEST TITLE", 5, 3,
                    ProblemVisibility.PRIVATE, scenarioInfos
            );
            problemId = problem.getId();
        }

        DetailedProblemInfo response = simpleUserService.getMyProblem(userId, problemId);

        assertThat(response).isNotNull();
        assertThat(response.problemId()).isEqualTo(problemId);
        assertThat(response.title()).isEqualTo(problem.getTitle());
        assertThat(response.description()).isEqualTo(problem.getDescription());
        assertThat(response.rewardMessage()).isEqualTo(problem.getRewardMessage());

        assertThat(response.numOfTotalScenarios()).isEqualTo(numOfScenarios);
        assertThat(response.numOfScenariosToGetReward())
                .isEqualTo(problem.getNumOfScenariosToGetReward());
        assertThat(response.numOfScenariosToFailPlay())
                .isEqualTo(problem.getNumOfScenariosToFailPlay());

        assertThat(response.visibility()).isEqualTo(problem.getVisibility());
        assertThat(response.createdAt())
                .isCloseTo(problem.getCreatedAt(), within(Duration.ofSeconds(5L)));
        assertThat(response.modifiedAt()).isNull();

        List<ScenarioInfo> responseInfoList = response.scenarioInfos();
        assertThat(responseInfoList).isNotNull().hasSize(numOfScenarios);
        assertThat(responseInfoList)
                .isSortedAccordingTo(Comparator.comparing(ScenarioInfo::getScenarioOrder));

        Map<Integer, ScenarioInfo> scenarioInfoMap = Arrays.stream(scenarioInfos)
                .collect(Collectors.toMap(ScenarioInfo::getScenarioOrder, Function.identity()));

        for (ScenarioInfo responseInfo : responseInfoList) {

            assertThat(responseInfo).isNotNull();

            int order = responseInfo.getScenarioOrder();
            assertThat(scenarioInfoMap).containsKey(order);

            ScenarioInfo scenarioInfo = scenarioInfoMap.get(order);
            assertThat(responseInfo).isEqualTo(scenarioInfo);
        }
    }

    @Test
    @DisplayName("스스로 작성한 평가 목록을 조회할 수 있다.")
    void getMyRatings() {
        Long userId = testUser.getId();

        int numOfRatings = 12;
        List<Rating> ratings;

        {
            ratings = new ArrayList<>(numOfRatings);
            ScenarioInfo[] scenarioInfo = new ScenarioInfo[]{scenarioInfoSample};

            for (int i = 0; i < numOfRatings; i++) {
                String pTitle = String.format("problem-%d", i);
                int n = i + 10;
                Problem problem = data.createNewProblem(
                        userId, pTitle, n, n, ProblemVisibility.PRIVATE, scenarioInfo
                );

                Long problemId = problem.getId();
                String comment = String.format("comment-%d", i);
                int score = i % 6;

                ratings.add(data.createNewRating(
                        problemId, userId, comment, score
                ));
            }
        }

        int pageNo = 0;
        int pageSize = ratings.size() / 2;

        SimplePageResponse<RatingInfo> response = simpleUserService.getMyRatings(
                userId, pageNo, pageSize
        );

        assertThat(response).isNotNull();
        assertThat(response.pageNoRequest()).isEqualTo(pageNo);
        assertThat(response.pageSizeRequest()).isEqualTo(pageSize);
        assertThat(response.numOfPagedElements()).isEqualTo(pageSize);
        assertThat(response.numOfTotalElements()).isEqualTo(numOfRatings);
        assertThat(response.hasNext()).isTrue();

        List<RatingInfo> pagedElements = response.pagedElements();
        assertThat(pagedElements).isNotNull().hasSize(pageSize);

        Map<Long, Rating> ratingMap = ratings.stream()
                .collect(Collectors.toMap(Rating::getId, Function.identity()));

        for (RatingInfo element : response.pagedElements()) {

            assertThat(element).isNotNull();

            Long ratingId = element.ratingId();
            assertThat(ratingMap).containsKey(ratingId);

            Rating rating = ratingMap.get(ratingId);
            assertThat(rating.getId()).isEqualTo(element.ratingId());
            assertThat(rating.getProblem().getId()).isEqualTo(element.problemId());
            assertThat(rating.getComment()).isEqualTo(element.comment());
            assertThat(rating.getScore()).isEqualTo(element.score());
            assertThat(rating.getCreatedAt())
                    .isCloseTo(element.createdAt(), within(Duration.ofSeconds(5L)));
            assertThat(rating.getModifiedAt()).isNull();
        }
    }

    @Test
    @DisplayName("스스로 작성한 평가 세부 내용을 조회할 수 있다.")
    void getMyRating() {
        Long userId = testUser.getId();

        Rating rating;
        Long ratingId;

        {
            Problem problem = data.createNewProblem(
                    userId, "testTITLE", 5, 5,
                    ProblemVisibility.PRIVATE,
                    new ScenarioInfo[]{scenarioInfoSample}
            );

            Long problemId = problem.getId();
            String comment = "TEST COMMENT";
            int score = 3;

            rating = data.createNewRating(problemId, userId, comment, score);
            ratingId = rating.getId();
        }

        RatingInfo response = simpleUserService.getMyRating(userId, ratingId);

        assertThat(response).isNotNull();
        assertThat(response.ratingId()).isEqualTo(ratingId);
        assertThat(response.problemId()).isEqualTo(rating.getProblem().getId());
        assertThat(response.comment()).isEqualTo(rating.getComment());
        assertThat(response.score()).isEqualTo(rating.getScore());
        assertThat(response.createdAt())
                .isCloseTo(rating.getCreatedAt(), within(Duration.ofSeconds(5L)));
        assertThat(response.modifiedAt()).isNull();
    }

    @Test
    @DisplayName("사용자를 찾을 수 없으면 NotFoundException 이 발생한다.")
    void testUserNotFoundException() {
        Long notExistingUserId = Long.MAX_VALUE;
        String tempStr = "SOMETHING";

        // 정보 조회, 탈퇴, 정보 수정, 설정 수정, 비번 바꾸기
        assertThatThrownBy(() -> simpleUserService.getMe(notExistingUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.withdrawMe(notExistingUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateMyInfo(
                notExistingUserId, tempStr, tempStr, tempStr
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateMySetting(notExistingUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateMyPassword(
                notExistingUserId, tempStr, tempStr
        ))
                .isInstanceOf(UserNotFoundException.class);

        // 내가 만든 문제 목록 보기, 문제 내용 보기
        assertThatThrownBy(() -> simpleUserService.getMyProblems(
                notExistingUserId, 0, 10
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.getMyProblem(notExistingUserId, null))
                .isInstanceOf(UserNotFoundException.class);

        // 내가 평가한 내용 목록 보기, 평가 내용 보기
        assertThatThrownBy(() -> simpleUserService.getMyRatings(
                notExistingUserId, 0, 10
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.getMyRating(notExistingUserId, null))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("탈퇴한 사용자는 식별되지 않는다.")
    void testUserNotFoundExceptionOnWithdrawnUser() {
        Long withdrawnUserId;
        Long withdrawnUserProblemId;
        Long withdrawnUserRatingId;
        String tempStr = "SOMETHING";

        {
            User newWithdrawnUser = data.createNewWithdrawnUser(
                    "testWITHDRAWN", "testWITHDRAWNEMAIL",
                    "testTHUMBNAIL", "testPW"
            );

            withdrawnUserId = newWithdrawnUser.getId();

            Problem problem = data.createNewProblem(
                    withdrawnUserId, "TITLE", 2, 3,
                    ProblemVisibility.PUBLIC,
                    new ScenarioInfo[]{scenarioInfoSample}
            );

            withdrawnUserProblemId = problem.getId();

            Rating rating = data.createNewRating(
                    withdrawnUserProblemId, withdrawnUserId,
                    "comment", 3
            );

            withdrawnUserRatingId = rating.getId();
        }

        // 정보 조회, 탈퇴, 정보 수정, 설정 수정, 비번 바꾸기
        assertThatThrownBy(() -> simpleUserService.getMe(withdrawnUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.withdrawMe(withdrawnUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateMyInfo(
                withdrawnUserId, tempStr, tempStr, tempStr
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateMySetting(withdrawnUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateMyPassword(
                withdrawnUserId, tempStr, tempStr
        ))
                .isInstanceOf(UserNotFoundException.class);

        // 내가 만든 문제 목록 보기, 문제 내용 보기
        assertThatThrownBy(() -> simpleUserService.getMyProblems(
                withdrawnUserId, 0, 10
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.getMyProblem(
                withdrawnUserId, withdrawnUserProblemId
        ))
                .isInstanceOf(UserNotFoundException.class);

        // 내가 평가한 내용 목록 보기, 평가 내용 보기
        assertThatThrownBy(() -> simpleUserService.getMyRatings(
                withdrawnUserId, 0, 10
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.getMyRating(
                withdrawnUserId, withdrawnUserRatingId
        ))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("자신이 소유하지 않은 자원은 찾을 수 없다.")
    void testNotOwnedResources() {
        Long userId = testUser.getId();
        int numOfEachResources = 5;

        {
            User anotherUser = data.createNewUser(
                    "another", null, null, "sample"
            );

            Long anotherUserId = anotherUser.getId();
            ScenarioInfo[] scenarioInfo = new ScenarioInfo[]{scenarioInfoSample};

            for (int i = 0; i < numOfEachResources; i++) {
                String title = String.format("title-%d", i);
                int nOfSToGetReward = i + 10;
                int nOfSToFailPlay = nOfSToGetReward / 2;

                ProblemVisibility visibility = i % 2 == 0 ?
                        ProblemVisibility.PRIVATE : ProblemVisibility.PUBLIC;

                Problem problem = data.createNewProblem(
                        anotherUserId, title, nOfSToGetReward, nOfSToFailPlay,
                        visibility, scenarioInfo
                );

                Long problemId = problem.getId();
                String comment = String.format("comment-%d", i);
                int score = i % 6;

                data.createNewRating(problemId, anotherUserId, comment, score);
            }
        }

        int pageNo = 0;
        int pageSize = numOfEachResources / 2;

        BiFunction<Integer, Integer, SimplePageResponse<?>> func1
                = (pn, ps) -> simpleUserService.getMyProblems(userId, pn, ps);
        BiFunction<Integer, Integer, SimplePageResponse<?>> func2
                = (pn, ps) -> simpleUserService.getMyRatings(userId, pn, ps);

        //noinspection unchecked
        BiFunction<Integer, Integer, SimplePageResponse<?>>[] functions
                = new BiFunction[]{func1, func2};

        for (var func : functions) {
            SimplePageResponse<?> response = func.apply(pageNo, pageSize);

            assertThat(response).isNotNull();
            assertThat(response.pageNoRequest()).isEqualTo(pageNo);
            assertThat(response.pageSizeRequest()).isEqualTo(pageSize);
            assertThat(response.numOfPagedElements()).isZero();
            assertThat(response.numOfTotalElements()).isZero();
            assertThat(response.hasNext()).isFalse();

            List<?> pagedElements1 = response.pagedElements();
            assertThat(pagedElements1).isNotNull().isEmpty();
        }
    }

    @Test
    @DisplayName("자신이 소유하지 않은 자원을 보려할 때 ForbiddenException 이 발생한다.")
    void testForbiddenException() {
        Long userId = testUser.getId();
        Long problemId;
        Long ratingId;

        {
            User anotherUser = data.createNewUser(
                    "another", null, null, "sample"
            );

            Long anotherUserId = anotherUser.getId();
            Problem problem = data.createNewProblem(
                    anotherUserId, "title", 1, 2,
                    ProblemVisibility.PUBLIC, new ScenarioInfo[]{scenarioInfoSample}
            );

            problemId = problem.getId();

            Rating rating = data.createNewRating(
                    problemId, anotherUserId, null, 3
            );
            ratingId = rating.getId();
        }

        assertThatThrownBy(() -> simpleUserService.getMyProblem(userId, problemId))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> simpleUserService.getMyRating(userId, ratingId))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 번호를 변경할 수 없다.")
    void testPasswordMismatchException() {
        Long userId = testUser.getId();

        assertThatThrownBy(() -> simpleUserService.updateMyPassword(
                userId, "invalid pw", "new pw"
        ))
                .isInstanceOf(PasswordMismatchException.class);
    }

    @Component
    @Transactional
    @SuppressWarnings("SameParameterValue")
    protected static class DataInitFacade {

        @Autowired
        GeneralDataInitializer initializer;

        @Autowired
        PasswordEncoder pwEncoder;

        @Autowired
        ScenarioInfoSerializer scenarioInfoSerializer;

        User createNewUser(
                String name, String email,
                String thumbnailUrl, String password
        ) {
            return initializer.createUser(
                    name, email, null, pwEncoder.encode(password), thumbnailUrl, false, null
            );
        }

        User createNewWithdrawnUser(
                String name, String email,
                String thumbnailUrl, String password
        ) {
            return initializer.createUser(
                    name, email, null, pwEncoder.encode(password), thumbnailUrl, true,
                    LocalDateTime.now()
            );
        }

        @SneakyThrows
        Problem createNewProblem(
                Long userId, String title,
                int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
                ProblemVisibility visibility, ScenarioInfo[] scenarioInfos
        ) {
            String serializedSIs = scenarioInfoSerializer.serialize(scenarioInfos);

            return initializer.createProblem(
                    userId, title, null, null, numOfScenariosToGetReward,
                    numOfScenariosToFailPlay, visibility, serializedSIs
            );
        }

        Rating createNewRating(
                Long problemId, Long userId, String comment, int score
        ) {
            return initializer.createRating(problemId, userId, comment, score);
        }

        protected void initAll() {
            initializer.initAll();
        }
    }

}