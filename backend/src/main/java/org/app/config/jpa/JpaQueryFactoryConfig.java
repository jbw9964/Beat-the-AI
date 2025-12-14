package org.app.config.jpa;

import com.querydsl.jpa.impl.*;
import jakarta.persistence.*;
import org.springframework.context.annotation.*;

@Configuration
public class JpaQueryFactoryConfig {

    @Bean
    public JPAQueryFactory queryFactory(EntityManager em) {
        return new JPAQueryFactory(em);
    }
}
