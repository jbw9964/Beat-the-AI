package org.app.user.service;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

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
import org.app.util.*;
import org.app.util.api.*;
import org.app.util.exception.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.context.event.*;
import org.springframework.transaction.annotation.*;

@RecordApplicationEvents
@Import(SimpleUserServiceTest.DataInitializer.class)
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
    DataInitializer dataInitializer;

    @Autowired
    UserRepository userRepo;

    @Autowired
    PasswordEncoder pwEncoder;

    @MockitoSpyBean
    DateTimeProvider dateTimeProvider;

    @BeforeEach
    void setUp() {
        applicationEvents.clear();
        testUser = dataInitializer.createNewUser(
                "test", "testEMAIL", "testTHUMBNAIL", testPassword
        );
    }

    @AfterEach
    void tearDown() {
        dataInitializer.initAll();
    }

    @Test
    @DisplayName("사용자는 탈퇴할 수 있다.")
    void withdrawUser() {
        Long userId = testUser.getId();

        LocalDate withdrawnAt = dateTimeProvider.localDateNow();
        doAnswer(invocation -> withdrawnAt)
                .when(dateTimeProvider).localDateNow();

        Long response = simpleUserService.withdrawUser(userId);

        assertThat(response).isNotNull().isEqualTo(userId);

        Optional<User> find = userRepo.findById(userId);
        assertThat(find).isNotEmpty();

        User get = find.get();
        assertThat(get.withdrawn()).isTrue();
        assertThat(get.getWithdrawnAt()).isNotNull().isEqualTo(withdrawnAt);
    }

    @Test
    @DisplayName("사용자 탈퇴시 이벤트가 발행된다.")
    void testWithdrawEvent() {
        Long userId = testUser.getId();
        long timeToAwaitEventPub = 5L;

        simpleUserService.withdrawUser(userId);

        await()
                .atMost(Duration.ofSeconds(timeToAwaitEventPub))
                .pollDelay(Duration.ofMillis(500L))
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
    void updateInfo() {
        Long userId = testUser.getId();
        String newName = "NEW NAME";
        String newEmail = "NEW EMAIL";
        String newThumbnail = "NEW THUMBNAIL";

        Long response = simpleUserService.updateInfo(userId, newName, newEmail, newThumbnail);

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
    void updateSetting() {
        // TODO : 사용자 설정 변경 구현 후 테스트 구성하기

        Long userId = testUser.getId();

        assertThatThrownBy(() -> simpleUserService.updateSetting(userId))
                .isInstanceOf(NotImplementedException.class);
    }

    @Test
    @DisplayName("사용자 비밀번호를 변경할 수 있다.")
    void updatePassword() {
        Long userId = testUser.getId();
        String newPassword = "NEW PASSWORD";

        Long response = simpleUserService.updatePassword(userId, testPassword, newPassword);

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

                Problem problem = dataInitializer.createNewProblem(
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

            problem = dataInitializer.createNewProblem(
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
                Problem problem = dataInitializer.createNewProblem(
                        userId, pTitle, n, n, ProblemVisibility.PRIVATE, scenarioInfo
                );

                Long problemId = problem.getId();
                String comment = String.format("comment-%d", i);
                int score = i % 6;

                ratings.add(dataInitializer.createNewRating(
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
            Problem problem = dataInitializer.createNewProblem(
                    userId, "testTITLE", 5, 5,
                    ProblemVisibility.PRIVATE,
                    new ScenarioInfo[]{scenarioInfoSample}
            );

            Long problemId = problem.getId();
            String comment = "TEST COMMENT";
            int score = 3;

            rating = dataInitializer.createNewRating(problemId, userId, comment, score);
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
        assertThatThrownBy(() -> simpleUserService.withdrawUser(notExistingUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateInfo(
                notExistingUserId, tempStr, tempStr, tempStr
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateSetting(notExistingUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updatePassword(
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
            LocalDate withdrawnDate = dateTimeProvider.localDateNow();

            User newWithdrawnUser = dataInitializer.createNewWithdrawnUser(
                    "testWITHDRAWN", "testWITHDRAWNEMAIL",
                    "testTHUMBNAIL", "testPW", withdrawnDate
            );

            withdrawnUserId = newWithdrawnUser.getId();

            Problem problem = dataInitializer.createNewProblem(
                    withdrawnUserId, "TITLE", 2, 3,
                    ProblemVisibility.PUBLIC,
                    new ScenarioInfo[]{scenarioInfoSample}
            );

            withdrawnUserProblemId = problem.getId();

            Rating rating = dataInitializer.createNewRating(
                    withdrawnUserProblemId, withdrawnUserId,
                    "comment", 3
            );

            withdrawnUserRatingId = rating.getId();
        }

        // 정보 조회, 탈퇴, 정보 수정, 설정 수정, 비번 바꾸기
        assertThatThrownBy(() -> simpleUserService.getMe(withdrawnUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.withdrawUser(withdrawnUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateInfo(
                withdrawnUserId, tempStr, tempStr, tempStr
        ))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updateSetting(withdrawnUserId))
                .isInstanceOf(UserNotFoundException.class);
        assertThatThrownBy(() -> simpleUserService.updatePassword(
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
            User anotherUser = dataInitializer.createNewUser(
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

                Problem problem = dataInitializer.createNewProblem(
                        anotherUserId, title, nOfSToGetReward, nOfSToFailPlay,
                        visibility, scenarioInfo
                );

                Long problemId = problem.getId();
                String comment = String.format("comment-%d", i);
                int score = i % 6;

                dataInitializer.createNewRating(problemId, anotherUserId, comment, score);
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
            User anotherUser = dataInitializer.createNewUser(
                    "another", null, null, "sample"
            );

            Long anotherUserId = anotherUser.getId();
            Problem problem = dataInitializer.createNewProblem(
                    anotherUserId, "title", 1, 2,
                    ProblemVisibility.PUBLIC, new ScenarioInfo[]{scenarioInfoSample}
            );

            problemId = problem.getId();

            Rating rating = dataInitializer.createNewRating(
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

        assertThatThrownBy(() -> simpleUserService.updatePassword(
                userId, "invalid pw", "new pw"
        ))
                .isInstanceOf(PasswordMismatchException.class);
    }

    @Component
    @SuppressWarnings("SameParameterValue")
    protected static class DataInitializer {

        @Autowired
        UserRepository userRepo;

        @Autowired
        PasswordEncoder pwEncoder;

        @Autowired
        UserProblemRepository userProblemRepo;

        @Autowired
        ScenarioInfoSerializer scenarioInfoSerializer;

        @Autowired
        UserRatingRepository userRatingRepo;

        @Transactional
        User createNewUser(
                String name, String email,
                String thumbnailUrl, String password
        ) {
            User user = new User(name);
            user.changeEmail(email);
            user.changeThumbnailUrl(thumbnailUrl);
            user.changeEncryptedPassword(pwEncoder.encode(password));
            return userRepo.save(user);
        }

        @Transactional
        User createNewWithdrawnUser(
                String name, String email,
                String thumbnailUrl, String password,
                LocalDate withdrawnAt
        ) {
            User user = new User(name);
            user.changeEmail(email);
            user.changeThumbnailUrl(thumbnailUrl);
            user.changeEncryptedPassword(pwEncoder.encode(password));
            user.withdrawUser(withdrawnAt);
            return userRepo.save(user);
        }

        @Transactional
        @SneakyThrows
        Problem createNewProblem(
                Long userId, String title,
                int numOfScenariosToGetReward, int numOfScenariosToFailPlay,
                ProblemVisibility visibility, ScenarioInfo[] scenarioInfos
        ) {
            User user = userRepo.findById(userId).orElseThrow(AssertionError::new);
            String serializedSIs = scenarioInfoSerializer.serialize(scenarioInfos);

            Problem problem = new Problem(
                    user, title, numOfScenariosToGetReward, numOfScenariosToFailPlay,
                    visibility, serializedSIs
            );

            return userProblemRepo.save(problem);
        }

        @Transactional
        Rating createNewRating(
                Long problemId, Long userId, String comment, int score
        ) {
            Problem problem = userProblemRepo.findById(problemId).orElseThrow(AssertionError::new);
            Rating rating = new Rating(problem, userId, score);
            rating.changeComment(comment);

            return userRatingRepo.save(rating);
        }

        @Transactional
        protected void initAll() {
            userRatingRepo.deleteAll();
            userProblemRepo.deleteAll();
            userRepo.deleteAll();
        }
    }

}