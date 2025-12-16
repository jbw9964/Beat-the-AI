package org.app.entity;

import jakarta.persistence.*;
import java.time.*;
import lombok.*;

@Getter
@Entity
@Table(
        name = "user",
        indexes = {
                @Index(
                        name = "UK__USER_LOGIN_ID",
                        columnList = "login_id",
                        unique = true
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends AuditingCreation implements SoftDelete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 30, nullable = false)
    private String name;

    @Column(length = 50)
    private String email;

    @Column(name = "login_id", length = 50, updatable = false)
    private String loginId;

    @Column(length = 255)
    private String encryptedPassword;

    @Column(length = 255)
    private String thumbnailUrl;

    // TODO : 유저 탈퇴 시 관련 자원 배치 삭제도 생각해야 함.
    @Embedded
    private ScheduledRemoval scheduledRemoval;

    public User(String name) {
        this(name, null, null);
    }

    public User(String name, String loginId, String encryptedPassword) {
        this.name = name;
        this.loginId = loginId;
        this.encryptedPassword = encryptedPassword;
        this.scheduledRemoval = ScheduledRemoval.notScheduled();
    }

    public void changeName(String name) {
        this.name = name;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changeThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void changeEncryptedPassword(String encryptedPw) {
        this.encryptedPassword = encryptedPw;
    }

    public void withdrawUser(
            LocalDateTime now, LocalDate scheduledRemovalDate
    ) {
        this.scheduledRemoval = ScheduledRemoval.scheduled(
                now, scheduledRemovalDate
        );
    }

    public boolean withdrawn() {
        return this.doesRemovalScheduled();
    }
}
