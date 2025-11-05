package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.*;

@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity extends AuditingCreation {

    @MockableLastModifiedDate
    private LocalDateTime modifiedAt;

    @PrePersist
    private void removeModifiedAtOnCreation() {
        if (modifiedAt != null) {
            modifiedAt = null;
        }
    }
}
