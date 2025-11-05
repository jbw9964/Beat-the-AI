package org.app.entity;

import java.lang.annotation.*;
import org.app.config.jpa.*;
import org.springframework.data.annotation.*;

/**
 * 테스트시 엔티티 수정 시각을 mock 하기 위한 annotation
 *
 * @see org.app.config.jpa.JpaAuditingConfig
 * @see EntityAuditingProvider
 * @see BaseTimeEntity
 */
@LastModifiedBy
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SuppressWarnings({"SpringModulithApiUsageInspection", "JavadocReference"})
public @interface MockableLastModifiedDate {

}
