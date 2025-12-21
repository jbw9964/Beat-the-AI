package org.app.config.domain.image.internal.invoker;

import static org.assertj.core.api.Assertions.*;

import java.util.*;
import org.*;
import org.app.config.domain.image.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;
import org.support.*;

@Import(DbStorageInvokingStrategyTest.DataInitFacade.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class DbStorageInvokingStrategyTest extends IntegrationTestSupport {

    @Autowired
    DbStorageInvokingStrategy strategy;

    @Autowired
    DataInitFacade data;

    @Autowired
    TestActualRewardImageRepository actualRewardImageRepo;

    @Autowired
    TestOverviewRewardImageRepository overviewRewardImageRepo;

    @AfterEach
    void tearDown() {
        data.initAll();
    }

    @Test
    @DisplayName("Db 저장 전략을 통해 실제 보상 이미지를 가져올 수 있다.")
    void getImageFrom1() {
        byte[] image = {1, 2, 3, 4, 5};

        ActualRewardImage entity = data.createActualRewardImage(image);

        byte[] response = strategy.getImageFrom(entity);

        assertThat(response).isNotNull();
        assertThat(Arrays.equals(image, response)).isTrue();
    }

    @Test
    @DisplayName("Db 저장 전략을 통해 보상 미리보기 이미지를 가져올 수 있다.")
    void getImageFrom2() {
        byte[] image = {1, 2, 3, 4, 5, 6, 7, 8};

        OverviewRewardImage entity = data.createOverviewRewardImage(image);

        byte[] response = strategy.getImageFrom(entity);

        assertThat(response).isNotNull();
        assertThat(Arrays.equals(image, response)).isTrue();
    }

    @Test
    @DisplayName("Db 저장 전략을 통해 이미지를 저장할 수 있다.")
    void saveRewardImages() {
        byte[] actualImage = {1, 2, 3, 4};
        byte[] overviewImage = {1, 2, 3, 4, 5, 6, 7, 8};

        RewardImageInfo response = strategy.saveRewardImages(actualImage, overviewImage);

        assertThat(response).isNotNull().hasNoNullFieldsOrProperties();

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

        assertThat(find1).isInstanceOf(DbStorageActualRewardImage.class);
        assertThat(find2).isInstanceOf(DbStorageOverviewRewardImage.class);

        DbStorageActualRewardImage casted1 = (DbStorageActualRewardImage) find1;
        DbStorageOverviewRewardImage casted2 = (DbStorageOverviewRewardImage) find2;

        assertThat(Arrays.equals(actualImage, casted1.getActualImage())).isTrue();
        assertThat(Arrays.equals(overviewImage, casted2.getOverviewImage())).isTrue();
    }

    @Test
    @DisplayName("Db 저장 전략을 통해 실제 보상 이미지를 삭제할 수 있다.")
    void delete1() {
        ActualRewardImage entity = data.createActualRewardImage(new byte[]{1, 2, 3});

        Long response = strategy.delete(entity);

        assertThat(response).isNotNull().isEqualTo(entity.getId());

        Optional<ActualRewardImage> find = actualRewardImageRepo.findById(response);
        assertThat(find).isEmpty();
    }

    @Test
    @DisplayName("Db 저장 전략을 통해 보상 미리보기 이미지를 삭제할 수 있다.")
    void delete2() {
        OverviewRewardImage entity = data.createOverviewRewardImage(new byte[]{1, 2, 3});

        Long response = strategy.delete(entity);

        assertThat(response).isNotNull().isEqualTo(entity.getId());

        Optional<OverviewRewardImage> find = overviewRewardImageRepo.findById(response);
        assertThat(find).isEmpty();
    }

    @Component
    @Transactional
    protected static class DataInitFacade {

        @Autowired
        GeneralDataInitializer initializer;

        ActualRewardImage createActualRewardImage(byte[] image) {
            return initializer.actualRewardImageBuilder()
                    .actualImage(image)
                    .storageType(RewardStorageType.DB)
                    .build();
        }

        OverviewRewardImage createOverviewRewardImage(byte[] image) {
            return initializer.overviewRewardImageBuilder()
                    .overviewImage(image)
                    .storageType(RewardStorageType.DB)
                    .build();
        }

        void initAll() {
            initializer.initAll();
        }
    }
}