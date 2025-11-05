package org.app.config.jpa;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import jakarta.persistence.*;
import jakarta.transaction.*;
import java.time.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.*;
import org.app.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.repository.config.*;
import org.springframework.stereotype.*;
import org.springframework.test.context.bean.override.mockito.*;

@Slf4j
@EnableJpaRepositories(
        basePackageClasses = Backend.class,
        considerNestedRepositories = true
)
@Import(EntityAuditingProviderTest.DataInitializer.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class EntityAuditingProviderTest extends IntegrationTestSupport {

    @MockitoSpyBean
    EntityAuditingProvider auditingProvider;

    @Autowired
    DataInitializer dataInitializer;

    @AfterEach
    void tearDown() {
        dataInitializer.initAll();
    }

    @Test
    @DisplayName("엔티티 생성 시각과 수정 시각을 mock 할 수 있다.")
    void testLocalDateTimeMocking() {
        Long id = 1L;
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime mockedCreatedAt = now.minusDays(3).minusHours(2);
        when(auditingProvider.provideLocalDateTime()).thenReturn(mockedCreatedAt);

        log.info("now: {}, mockedCreatedAt: {}", now, mockedCreatedAt);

        String first = "first";
        TestEntity created = dataInitializer.createNew(id, first);
        log.info("created: {}", created);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo(id);
        assertThat(created.getName()).isEqualTo(first);
        assertThat(created.getCreatedAt()).isEqualTo(mockedCreatedAt);
        assertThat(created.getModifiedAt()).isNull();

        LocalDateTime mockedModifiedAt = mockedCreatedAt.minusDays(3).minusHours(2);
        when(auditingProvider.provideLocalDateTime()).thenReturn(mockedModifiedAt);

        log.info("now: {}, mockedModifiedAt: {}", now, mockedModifiedAt);

        String second = "second";
        TestEntity modified = dataInitializer.changeName(id, second);
        log.info("modified: {}", modified);

        assertThat(modified).isNotNull();
        assertThat(modified.getId()).isEqualTo(id);
        assertThat(modified.getName()).isEqualTo(second);
        assertThat(modified.getCreatedAt()).isEqualTo(mockedCreatedAt);
        assertThat(modified.getModifiedAt()).isEqualTo(mockedModifiedAt);
    }

    private interface TestEntityRepo extends JpaRepository<TestEntity, Long> {

    }

    @Component
    protected static class DataInitializer {

        @Autowired
        TestEntityRepo testEntityRepo;

        @Transactional
        TestEntity createNew(Long id, String name) {
            TestEntity entity = new TestEntity(id, name);
            return testEntityRepo.save(entity);
        }

        @Transactional
        TestEntity changeName(Long id, String newName) {
            TestEntity entity = testEntityRepo.findById(id).orElseThrow(AssertionError::new);
            entity.setName(newName);
            return entity;
        }

        @Transactional
        void initAll() {
            testEntityRepo.deleteAll();
        }
    }

    @Entity
    @Getter
    @Setter
    @ToString
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    private static class TestEntity extends BaseTimeEntity {

        @Id
        private Long id;

        private String name;

        public TestEntity(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
