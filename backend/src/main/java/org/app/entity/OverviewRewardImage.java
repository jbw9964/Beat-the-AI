package org.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "storage_type")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OverviewRewardImage extends AuditingCreation
        implements StorageType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
