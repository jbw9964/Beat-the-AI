package org.app.config.domain.image.internal.invoker;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.nio.file.*;
import java.time.*;
import java.util.*;
import org.*;
import org.app.config.domain.image.*;
import org.app.config.domain.image.internal.*;
import org.app.config.domain.image.internal.event.*;
import org.app.config.domain.image.internal.repository.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.bean.override.mockito.*;
import org.springframework.test.context.event.*;
import org.springframework.transaction.annotation.*;
import org.support.*;


@RecordApplicationEvents
@Import(ServerStorageInvokingStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class ServerStorageInvokingStrategyTest extends IntegrationTestSupport {

    static Path storageBasePath;

    @Value("${reward-image.server-storing-abs-location}")
    String rewardImageStorageBaseDirectory;

    @Autowired
    ServerStorageInvokingStrategy strategy;

    @MockitoSpyBean
    MockableFiles mockedFiles;

    @MockitoSpyBean
    UuidProvider mockedUidProvider;

    @MockitoSpyBean
    EntityEditor mockedEntityEditor;

    @Autowired
    ApplicationEvents applicationEvents;

    @Autowired
    DataInitFacade data;

    @Autowired
    TestActualRewardImageRepository actualRewardImageRepo;

    @Autowired
    TestOverviewRewardImageRepository overviewRewardImageRepo;

    @BeforeEach
    void setUp() {
        storageBasePath = Paths.get(rewardImageStorageBaseDirectory);
        applicationEvents.clear();
    }

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("Server 저장 전략을 통해 실제 보상 이미지를 가져올 수 있다.")
    void getImageFrom1() throws IOException {
        byte[] image = {1, 2, 3};
        Path filePath = storageBasePath.resolve("Testing1");

        doAnswer(invocation -> true)
                .when(mockedFiles)
                .exists(filePath);
        doAnswer(invocation -> image)
                .when(mockedFiles)
                .readAllBytes(filePath);

        ActualRewardImage entity = data.createActualRewardImage(
                filePath.toAbsolutePath().toString()
        );

        assertThatCode(() -> strategy.getImageFrom(entity))
                .doesNotThrowAnyException();

        verify(mockedFiles, times(1))
                .readAllBytes(filePath);
    }

    @Test
    @DisplayName("Server 저장 전략을 통해 보상 미리보기 이미지를 가져올 수 있다.")
    void getImageFrom2() throws IOException {
        byte[] image = {1, 2, 3};
        Path filePath = storageBasePath.resolve("Testing2");

        doAnswer(invocation -> true)
                .when(mockedFiles)
                .exists(filePath);
        doAnswer(invocation -> image)
                .when(mockedFiles)
                .readAllBytes(filePath);

        OverviewRewardImage entity = data.createOverviewRewardImage(
                filePath.toAbsolutePath().toString()
        );

        assertThatCode(() -> strategy.getImageFrom(entity))
                .doesNotThrowAnyException();

        verify(mockedFiles, times(1))
                .readAllBytes(filePath);
    }

    @Test
    @DisplayName("Server 저장 전략을 통해 이미지를 저장할 수 있다.")
    void saveRewardImages() throws IOException {
        byte[] image1 = {1, 2, 3};
        byte[] image2 = {1, 2, 3, 4, 5};

        Path filePath1 = storageBasePath.resolve("ttt1");
        Path filePath2 = storageBasePath.resolve("ttt2");

        doAnswer(invocation -> {
            byte[] image = invocation.getArgument(1, byte[].class);

            boolean eq1 = Arrays.equals(image, image1);
            boolean eq2 = Arrays.equals(image, image2);

            if (!eq1 && !eq2) {
                throw new RuntimeException("Unexpected image given");
            }

            return eq1 ? filePath1 : filePath2;
        })
                .when(mockedFiles)
                .write(any(), any());

        RewardImageInfo response = strategy.saveRewardImages(
                image1, image2
        );

        assertThat(response).isNotNull().hasNoNullFieldsOrProperties();

        verify(mockedFiles, times(1))
                .write(any(), AdditionalMatchers.aryEq(image1));
        verify(mockedFiles, times(1))
                .write(any(), AdditionalMatchers.aryEq(image2));

        Long actualRewardImageId = response.actualRewardImageId();
        Long overviewRewardImageId = response.overviewRewardImageId();

        Optional<ActualRewardImage> opt1 = actualRewardImageRepo.findById(
                actualRewardImageId
        );
        Optional<OverviewRewardImage> opt2 = overviewRewardImageRepo.findById(
                overviewRewardImageId
        );

        assertThat(opt1).isPresent();
        assertThat(opt2).isPresent();

        ActualRewardImage find1 = opt1.get();
        OverviewRewardImage find2 = opt2.get();

        assertThat(find1).isInstanceOf(ServerStorageActualRewardImage.class);
        assertThat(find2).isInstanceOf(ServerStorageOverviewRewardImage.class);

        ServerStorageActualRewardImage casted1 = (ServerStorageActualRewardImage) find1;
        ServerStorageOverviewRewardImage casted2 = (ServerStorageOverviewRewardImage) find2;

        assertThat(casted1.getActualImagePath()).isEqualTo(
                filePath1.toAbsolutePath().toString()
        );
        assertThat(casted2.getOverviewImagePath()).isEqualTo(
                filePath2.toAbsolutePath().toString()
        );
    }

    @Test
    @DisplayName("Server 저장 전략을 통해 실제 보상 이미지를 예약 삭제시킬 수 있다.")
    void delete1() {
        String filePath = "temp1";
        ActualRewardImage entity = data.createActualRewardImage(filePath);

        Long response = strategy.delete(entity);

        assertThat(response).isNotNull().isEqualTo(entity.getId());

        ActualRewardImage find = actualRewardImageRepo.findById(response)
                .orElseThrow(AssertionError::new);
        ServerStorageActualRewardImage casted = (ServerStorageActualRewardImage) find;

        assertThat(casted.doesRemovalScheduled()).isTrue();

        ScheduledRemoval scheduledRemoval = casted.getScheduledRemoval();
        assertThat(scheduledRemoval).isNotNull();
        assertThat(scheduledRemoval.requestedAt()).isNotNull();
        assertThat(scheduledRemoval.scheduedAt()).isNotNull();
        assertThat(scheduledRemoval.doesRemovalScheduled()).isTrue();
    }

    @Test
    @DisplayName("Server 저장 전략을 통해 보상 미리보기 이미지를 예약 삭제시킬 수 있다.")
    void delete2() {
        String filePath = "temp2";
        OverviewRewardImage entity = data.createOverviewRewardImage(filePath);

        Long response = strategy.delete(entity);

        assertThat(response).isNotNull().isEqualTo(entity.getId());

        OverviewRewardImage find = overviewRewardImageRepo.findById(response)
                .orElseThrow(AssertionError::new);
        ServerStorageOverviewRewardImage casted = (ServerStorageOverviewRewardImage) find;

        assertThat(casted.doesRemovalScheduled()).isTrue();

        ScheduledRemoval scheduledRemoval = casted.getScheduledRemoval();
        assertThat(scheduledRemoval).isNotNull();
        assertThat(scheduledRemoval.requestedAt()).isNotNull();
        assertThat(scheduledRemoval.scheduedAt()).isNotNull();
        assertThat(scheduledRemoval.doesRemovalScheduled()).isTrue();
    }

    @Test
    @DisplayName("삭제 예정인 이미지를 조회, 삭제하려 할 때 NotFoundException 이 발생한다.")
    void testNotFoundException() {
        ServerStorageActualRewardImage softDeletedActualRewardImage
                = data.createSoftDeletedActualRewardImage();
        ServerStorageOverviewRewardImage softDeletedOverviewRewardImage
                = data.createSoftDeletedOverviewRewardImage();

        // 삭제 예정인데 조회 시도할 때
        assertThatThrownBy(() -> strategy.getImageFrom(softDeletedActualRewardImage))
                .isInstanceOf(ImageNotFoundException.class);
        assertThatThrownBy(() -> strategy.getImageFrom(softDeletedOverviewRewardImage))
                .isInstanceOf(ImageNotFoundException.class);

        // 이미 삭제 예정인데 삭제 시도할 때
        assertThatThrownBy(() -> strategy.delete(softDeletedActualRewardImage))
                .isInstanceOf(ImageNotFoundException.class);
        assertThatThrownBy(() -> strategy.delete(softDeletedOverviewRewardImage))
                .isInstanceOf(ImageNotFoundException.class);
    }

    @Test
    @DisplayName("이미지 조회시 위치 정보는 DB 에 존재하지만 실제 서버 저장소에 없으면 "
                 + "FailedToFindImageOnServerException 이 발생한다.")
    void testFailedToFindImageOnServerException() {
        Path nonExistingFilePath = storageBasePath.resolve("i don't exists");
        String nonExistingAbsPath = nonExistingFilePath.toAbsolutePath().toString();

        doAnswer(invocation -> false)
                .when(mockedFiles)
                .exists(nonExistingFilePath);

        ActualRewardImage actualRewardImage
                = data.createActualRewardImage(nonExistingAbsPath);
        OverviewRewardImage overviewRewardImage
                = data.createOverviewRewardImage(nonExistingAbsPath);

        assertThatThrownBy(() -> strategy.getImageFrom(actualRewardImage))
                .isInstanceOf(FailedToFindImageOnServerException.class);
        assertThatThrownBy(() -> strategy.getImageFrom(overviewRewardImage))
                .isInstanceOf(FailedToFindImageOnServerException.class);
    }

    @Test
    @DisplayName("이미지 조회시 실제 이미지를 읽어들이다 에러가 발생하면 "
                 + "FailedToGetFileOnServerException 이 발생한다.")
    void testFailedToGetFileOnServerException() throws IOException {
        Path existingFilePath = storageBasePath.resolve("i exists.");
        String existingAbsPath = existingFilePath.toAbsolutePath().toString();

        doAnswer(invocation -> true)
                .when(mockedFiles)
                .exists(existingFilePath);

        doThrow(new IOException("U STUPID"))
                .when(mockedFiles)
                .readAllBytes(existingFilePath);

        ActualRewardImage actualRewardImage
                = data.createActualRewardImage(existingAbsPath);
        OverviewRewardImage overviewRewardImage
                = data.createOverviewRewardImage(existingAbsPath);

        assertThatThrownBy(() -> strategy.getImageFrom(actualRewardImage))
                .isInstanceOf(FailedToGetFileOnServerException.class);
        assertThatThrownBy(() -> strategy.getImageFrom(overviewRewardImage))
                .isInstanceOf(FailedToGetFileOnServerException.class);
    }

    @Test
    @DisplayName("이미지 저장시 IO 에러가 발생하거나 DB 단에서 문제가 발생하면 "
                 + "FailedToSaveImageToServerException 이 발생한다.")
    void testFailedToSaveImageToServerException() throws IOException {
        byte[] image = {1, 2, 3, 4, 5, 6};

        // 이미지 저장하다 에러 터졌을 때
        String alreadyExistingFileName = "i already exists.";
        Path alreadyExsitingFilePath = storageBasePath.resolve(alreadyExistingFileName);

        doReturn(alreadyExistingFileName)
                .when(mockedUidProvider)
                .getRandomUuidAsString();

        doThrow(new IOException())
                .when(mockedFiles)
                .write(alreadyExsitingFilePath, image);

        assertThatThrownBy(() -> strategy.saveRewardImages(image, image))
                .isInstanceOf(FailedToSaveImageToServerException.class);

        // DB 단에서 에러 터졌을 때
        String newFileName = "i'm new.";
        Path newFilePath = storageBasePath.resolve(newFileName);

        doReturn(newFileName)
                .when(mockedUidProvider)
                .getRandomUuidAsString();

        doReturn(newFilePath)
                .when(mockedFiles)
                .write(newFilePath, image);

        doThrow(new RuntimeException())
                .when(mockedEntityEditor)
                .saveEntities(any(), any());

        assertThatThrownBy(() -> strategy.saveRewardImages(image, image))
                .isInstanceOf(FailedToSaveImageToServerException.class);
    }

    @Test
    @DisplayName("이미지 저장시 에러가 발생하면 서버 이미지를 삭제하는 event 가 발생한다.")
    void testRemoveOrphanServerFileEvent() throws IOException {
        long timeToWaitEventNotPubed = 2L;
        long timeToWaitEventValidationSec = 5L;
        long pollDelayMs = 50L;

        byte[] image1 = {1, 2, 3};
        byte[] image2 = {4, 5, 6};

        String fileName1 = "i'm file 1.";
        String fileName2 = "i'm file 2.";
        Path filePath1 = storageBasePath.resolve(fileName1);
        Path filePath2 = storageBasePath.resolve(fileName2);

        // 아무 이미지도 저장 안되었을 시 event 는 발행되지 않는다.
        {
            doReturn(fileName1)
                    .when(mockedUidProvider)
                    .getRandomUuidAsString();

            doThrow(new IOException())
                    .when(mockedFiles)
                    .write(any(), any());

            assertThatThrownBy(() -> strategy.saveRewardImages(image1, image2))
                    .isInstanceOf(FailedToSaveImageToServerException.class);

            await()
                    .during(Duration.ofSeconds(timeToWaitEventNotPubed))
                    .pollDelay(Duration.ofMillis(50L))
                    .untilAsserted(() -> {
                        long count = applicationEvents.stream(RemoveOrphanServerFileEvent.class)
                                .count();

                        assertThat(count).isZero();
                    });

            applicationEvents.clear();
        }

        // 이미지 저장중 에러가 발생하면 저장된 이미지에 대해 event 가 발생한다.
        // 첫번째 이미지만 저장 되었을 때 (두번째 이미지 저장하다 IO 터짐)
        {
            doReturn(fileName1).doReturn(fileName2)
                    .when(mockedUidProvider)
                    .getRandomUuidAsString();

            doReturn(filePath1).doThrow(new IOException())
                    .when(mockedFiles)
                    .write(any(), any());

            assertThatThrownBy(() -> strategy.saveRewardImages(image1, image2))
                    .isInstanceOf(FailedToSaveImageToServerException.class);

            await()
                    .atMost(Duration.ofSeconds(timeToWaitEventValidationSec))
                    .pollInterval(Duration.ofMillis(pollDelayMs))
                    .untilAsserted(() -> {
                        List<RemoveOrphanServerFileEvent> events
                                = applicationEvents.stream(RemoveOrphanServerFileEvent.class)
                                .toList();

                        assertThat(events).hasSize(1);

                        RemoveOrphanServerFileEvent event = events.getFirst();
                        assertThat(events).isNotNull();

                        List<String> removals = event.removals();
                        assertThat(removals).isNotNull();

                        // 저장된 첫번째 이미지에 대해서만 이벤트가 존재한다.
                        assertThat(removals).containsExactly(
                                filePath1.toAbsolutePath().toString()
                        );
                    });

            applicationEvents.clear();
        }

        // 두번째 이미지까지 저장 되었을 때 (DB 단에서 저장하다 에러 터졌을 때)
        {
            doReturn(fileName1).doReturn(fileName2)
                    .when(mockedUidProvider)
                    .getRandomUuidAsString();

            doReturn(filePath1).doReturn(filePath2)
                    .when(mockedFiles)
                    .write(any(), any());

            doThrow(new RuntimeException())
                    .when(mockedEntityEditor)
                    .saveEntities(any(), any());

            assertThatThrownBy(() -> strategy.saveRewardImages(image1, image2))
                    .isInstanceOf(FailedToSaveImageToServerException.class);

            await()
                    .atMost(Duration.ofSeconds(timeToWaitEventValidationSec))
                    .pollInterval(Duration.ofMillis(pollDelayMs))
                    .untilAsserted(() -> {
                        List<RemoveOrphanServerFileEvent> events
                                = applicationEvents.stream(RemoveOrphanServerFileEvent.class)
                                .toList();

                        assertThat(events).hasSize(1);

                        RemoveOrphanServerFileEvent event = events.getFirst();
                        assertThat(events).isNotNull();

                        List<String> removals = event.removals();
                        assertThat(removals).isNotNull();

                        // 저장된 모든 이미지에 대해서 이벤트가 존재한다.
                        assertThat(removals).containsExactlyInAnyOrder(
                                filePath1.toAbsolutePath().toString(),
                                filePath2.toAbsolutePath().toString()
                        );
                    });
        }
    }

    @Component
    @Transactional
    protected static class DataInitFacade {

        @Autowired
        GeneralDataInitializer initializer;

        ActualRewardImage createActualRewardImage(String path) {
            return initializer.actualRewardImageBuilder()
                    .actualImagePath(path)
                    .storageType(RewardStorageType.SERVER)
                    .build();
        }

        ServerStorageActualRewardImage createSoftDeletedActualRewardImage() {
            String randomPath = UUID.randomUUID().toString();

            ActualRewardImage build = initializer.actualRewardImageBuilder()
                    .actualImagePath(randomPath)
                    .storageType(RewardStorageType.SERVER)
                    .build();

            ServerStorageActualRewardImage casted = (ServerStorageActualRewardImage) build;
            LocalDateTime requestedAt = LocalDateTime.now();
            LocalDate removalDate = LocalDate.now();

            casted.reserveRemoval(requestedAt, removalDate);

            return casted;
        }

        OverviewRewardImage createOverviewRewardImage(String path) {
            return initializer.overviewRewardImageBuilder()
                    .overviewImagePath(path)
                    .storageType(RewardStorageType.SERVER)
                    .build();
        }

        ServerStorageOverviewRewardImage createSoftDeletedOverviewRewardImage() {
            String randomPath = UUID.randomUUID().toString();

            OverviewRewardImage build = initializer.overviewRewardImageBuilder()
                    .overviewImagePath(randomPath)
                    .storageType(RewardStorageType.SERVER)
                    .build();

            ServerStorageOverviewRewardImage casted = (ServerStorageOverviewRewardImage) build;
            LocalDateTime requestedAt = LocalDateTime.now();
            LocalDate removalDate = LocalDate.now();

            casted.reserveRemoval(requestedAt, removalDate);

            return casted;
        }

        void initAll() {
            initializer.initAll();
        }
    }
}