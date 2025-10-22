package org.app;

import static org.assertj.core.api.Assertions.*;

import com.querydsl.jpa.impl.*;
import jakarta.persistence.*;
import jakarta.transaction.*;
import org.*;
import org.app.entity.*;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.*;

public class QueryDSLTest extends IntegrationTestSupport {

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    EntityManager em;

    @Test
    @Transactional
    @DisplayName("QueryDSL 이 정상 작동한다.")
    public void testQueryDSL() {

        User testUser = new User("name");
        em.persist(testUser);
        em.flush();

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        User find = queryFactory.selectFrom(QUser.user)
                .fetchOne();

        assertThat(find).isNotNull().isEqualTo(testUser);
        assertThat(find.getId()).isNotNull().isEqualTo(testUser.getId());

        em.createQuery("DELETE FROM User").executeUpdate();
    }
}



