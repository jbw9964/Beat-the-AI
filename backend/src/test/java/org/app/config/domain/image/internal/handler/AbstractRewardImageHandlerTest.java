package org.app.config.domain.image.internal.handler;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import java.util.stream.*;
import org.app.config.domain.image.*;
import org.app.config.domain.image.internal.*;
import org.app.config.domain.image.internal.repository.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.mockito.verification.*;

class AbstractRewardImageHandlerTest {

    ActualRewardImageRepository actualImageRepo;

    OverviewRewardImageRepository overviewImageRepo;

    RewardImageInvokerStrategy dbStrategy;

    RewardImageInvokerStrategy serverStrategy;

    RewardImageInvokerStrategy awsS3Strategy;

    AbstractRewardImageHandler handler;

    private static Stream<Arguments> getRewardStorageTypes() {
        return Stream.of(
                Arrays.stream(RewardStorageType.values())
                        .map(Arguments::of)
                        .toArray(Arguments[]::new)
        );
    }

    private AbstractRewardImageHandler createHandlerWithSavingStorageType(
            RewardStorageType savingStorageType
    ) {
        return new AbstractRewardImageHandler(
                actualImageRepo, overviewImageRepo,
                List.of(dbStrategy, serverStrategy, awsS3Strategy)
        ) {
            @Override
            public RewardStorageType savingStorageType() {
                return savingStorageType;
            }
        };
    }

    @BeforeEach
    void setUp() {
        actualImageRepo = mock(ActualRewardImageRepository.class);
        overviewImageRepo = mock(OverviewRewardImageRepository.class);
        dbStrategy = mock(RewardImageInvokerStrategy.class);
        serverStrategy = mock(RewardImageInvokerStrategy.class);
        awsS3Strategy = mock(RewardImageInvokerStrategy.class);

        when(dbStrategy.handleableType())
                .thenReturn(RewardStorageType.DB);
        when(serverStrategy.handleableType())
                .thenReturn(RewardStorageType.SERVER);
        when(awsS3Strategy.handleableType())
                .thenReturn(RewardStorageType.AWS_S3);
    }

    @ParameterizedTest
    @MethodSource("getRewardStorageTypes")
    @DisplayName("실제 보상 조회시 엔티티 저장 타입에 따라 올바른 전략이 호출된다.")
    void getActualRewardImage(RewardStorageType storageType) {
        handler = createHandlerWithSavingStorageType(null);

        Long id = 1L;
        ActualRewardImage mockedEntityResp = switch (storageType) {
            case DB -> new DbStorageActualRewardImage(null);
            case SERVER -> new ServerStorageActualRewardImage(null);
            case AWS_S3 -> new AwsS3ActualRewardImage(null);
        };

        when(actualImageRepo.findById(id))
                .thenReturn(Optional.of(mockedEntityResp));

        handler.getActualRewardImage(id);

        VerificationMode verifyDb, verifyServer, verifyAwsS3;

        switch (storageType) {
            case DB: {
                verifyDb = times(1);
                verifyServer = never();
                verifyAwsS3 = never();
                break;
            }
            case SERVER: {
                verifyDb = never();
                verifyServer = times(1);
                verifyAwsS3 = never();
                break;
            }
            case AWS_S3: {
                verifyDb = never();
                verifyServer = never();
                verifyAwsS3 = times(1);
                break;
            }
            default:
                throw new AssertionError("Unknown storageType");
        }

        verify(dbStrategy, verifyDb)
                .getImageFrom(mockedEntityResp);
        verify(serverStrategy, verifyServer)
                .getImageFrom(mockedEntityResp);
        verify(awsS3Strategy, verifyAwsS3)
                .getImageFrom(mockedEntityResp);
    }

    @ParameterizedTest
    @MethodSource("getRewardStorageTypes")
    @DisplayName("보상 미리보기 조회시 엔티티 저장 타입에 따라 올바른 전략이 호출된다.")
    void getOverviewRewardImage(RewardStorageType storageType) {
        handler = createHandlerWithSavingStorageType(null);

        Long id = 1L;
        OverviewRewardImage mockedEntityResp = switch (storageType) {
            case DB -> new DbStorageOverviewRewardImage(null);
            case SERVER -> new ServerStorageOverviewRewardImage(null);
            case AWS_S3 -> new AwsS3OverviewRewardImage(null);
        };

        when(overviewImageRepo.findById(id))
                .thenReturn(Optional.of(mockedEntityResp));

        handler.getOverviewRewardImage(id);

        VerificationMode verifyDb, verifyServer, verifyAwsS3;

        switch (storageType) {
            case DB: {
                verifyDb = times(1);
                verifyServer = never();
                verifyAwsS3 = never();
                break;
            }
            case SERVER: {
                verifyDb = never();
                verifyServer = times(1);
                verifyAwsS3 = never();
                break;
            }
            case AWS_S3: {
                verifyDb = never();
                verifyServer = never();
                verifyAwsS3 = times(1);
                break;
            }
            default:
                throw new AssertionError("Unknown storageType");
        }

        verify(dbStrategy, verifyDb)
                .getImageFrom(mockedEntityResp);
        verify(serverStrategy, verifyServer)
                .getImageFrom(mockedEntityResp);
        verify(awsS3Strategy, verifyAwsS3)
                .getImageFrom(mockedEntityResp);
    }

    @ParameterizedTest
    @MethodSource("getRewardStorageTypes")
    @DisplayName("보상 생성시 설정된 저장 타입에 따라 올바른 전략이 호출된다.")
    void saveRewardImages(RewardStorageType savingStorageType) {
        handler = createHandlerWithSavingStorageType(savingStorageType);

        byte[] one, two;
        one = two = new byte[0];

        handler.saveRewardImages(one, two);

        VerificationMode verifyDb, verifyServer, verifyAwsS3;

        switch (savingStorageType) {
            case DB: {
                verifyDb = times(1);
                verifyServer = never();
                verifyAwsS3 = never();
                break;
            }
            case SERVER: {
                verifyDb = never();
                verifyServer = times(1);
                verifyAwsS3 = never();
                break;
            }
            case AWS_S3: {
                verifyDb = never();
                verifyServer = never();
                verifyAwsS3 = times(1);
                break;
            }
            default:
                throw new AssertionError("Unknown storageType");
        }

        verify(dbStrategy, verifyDb)
                .saveRewardImages(one, two);
        verify(serverStrategy, verifyServer)
                .saveRewardImages(one, two);
        verify(awsS3Strategy, verifyAwsS3)
                .saveRewardImages(one, two);
    }

    @ParameterizedTest
    @MethodSource("getRewardStorageTypes")
    @DisplayName("보상 삭제시 엔티티 저장 타입에 따라 올바른 전략이 호출된다.")
    void deleteRewardImages(RewardStorageType storageType) {
        handler = createHandlerWithSavingStorageType(null);

        Long id = 1L;
        ActualRewardImage mockedEntityResp1;
        OverviewRewardImage mockedEntityResp2;

        switch (storageType) {
            case DB: {
                mockedEntityResp1 = new DbStorageActualRewardImage(null);
                mockedEntityResp2 = new DbStorageOverviewRewardImage(null);
                break;
            }
            case SERVER: {
                mockedEntityResp1 = new ServerStorageActualRewardImage(null);
                mockedEntityResp2 = new ServerStorageOverviewRewardImage(null);
                break;
            }
            case AWS_S3: {
                mockedEntityResp1 = new AwsS3ActualRewardImage(null);
                mockedEntityResp2 = new AwsS3OverviewRewardImage(null);
                break;
            }
            default:
                throw new AssertionError("Unknown storageType");
        }

        when(actualImageRepo.findById(id))
                .thenReturn(Optional.of(mockedEntityResp1));
        when(overviewImageRepo.findById(id))
                .thenReturn(Optional.of(mockedEntityResp2));

        RewardImageInfo info = new RewardImageInfo(id, id);
        handler.deleteRewardImages(info);

        VerificationMode verifyDb, verifyServer, verifyAwsS3;

        switch (storageType) {
            case DB: {
                verifyDb = times(1);
                verifyServer = never();
                verifyAwsS3 = never();
                break;
            }
            case SERVER: {
                verifyDb = never();
                verifyServer = times(1);
                verifyAwsS3 = never();
                break;
            }
            case AWS_S3: {
                verifyDb = never();
                verifyServer = never();
                verifyAwsS3 = times(1);
                break;
            }
            default:
                throw new AssertionError("Unknown storageType");
        }

        verify(dbStrategy, verifyDb)
                .delete(mockedEntityResp1);
        verify(dbStrategy, verifyDb)
                .delete(mockedEntityResp2);

        verify(serverStrategy, verifyServer)
                .delete(mockedEntityResp1);
        verify(serverStrategy, verifyServer)
                .delete(mockedEntityResp2);

        verify(awsS3Strategy, verifyAwsS3)
                .delete(mockedEntityResp1);
        verify(awsS3Strategy, verifyAwsS3)
                .delete(mockedEntityResp2);
    }

    @Test
    @DisplayName("엔티티가 존재하지 않으면 handler 단에서 NotFoundException 이 발생한다.")
    void testNotFoundException() {
        handler = createHandlerWithSavingStorageType(null);

        Long notExistingId = 1L;
        Long existingId = 2L;

        ActualRewardImage actualRewardImage = new ActualRewardImage() {
            @Override
            public RewardStorageType getStorageType() {
                return null;
            }
        };
        OverviewRewardImage overviewRewardImage = new OverviewRewardImage() {
            @Override
            public RewardStorageType getStorageType() {
                return null;
            }
        };

        when(actualImageRepo.findById(notExistingId))
                .thenReturn(Optional.empty());
        when(overviewImageRepo.findById(notExistingId))
                .thenReturn(Optional.empty());

        when(actualImageRepo.findById(existingId))
                .thenReturn(Optional.of(actualRewardImage));
        when(overviewImageRepo.findById(existingId))
                .thenReturn(Optional.of(overviewRewardImage));

        assertThatThrownBy(() -> handler.getActualRewardImage(notExistingId))
                .isInstanceOf(ImageNotFoundException.class);
        assertThatThrownBy(() -> handler.getOverviewRewardImage(notExistingId))
                .isInstanceOf(ImageNotFoundException.class);

        RewardImageInfo info1 = new RewardImageInfo(notExistingId, notExistingId);
        RewardImageInfo info2 = new RewardImageInfo(existingId, notExistingId);
        RewardImageInfo info3 = new RewardImageInfo(notExistingId, existingId);

        assertThatThrownBy(() -> handler.deleteRewardImages(info1))
                .isInstanceOf(ImageNotFoundException.class);
        assertThatThrownBy(() -> handler.deleteRewardImages(info2))
                .isInstanceOf(ImageNotFoundException.class);
        assertThatThrownBy(() -> handler.deleteRewardImages(info3))
                .isInstanceOf(ImageNotFoundException.class);
    }
}