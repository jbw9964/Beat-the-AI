package org.app.entity;

import java.lang.annotation.*;
import org.app.config.jpa.*;
import org.springframework.data.annotation.*;

/**
 * 테스트시 엔티티 생성 시각을 mock 하기 위한 annotation
 *
 * @see org.app.config.jpa.JpaAuditingConfig
 * @see EntityAuditingProvider
 * @see AuditingCreation
 */
@CreatedBy
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SuppressWarnings({"SpringModulithApiUsageInspection", "JavadocReference"})
public @interface MockableCreatedDate {

}
