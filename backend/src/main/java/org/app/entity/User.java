package org.app.entity;

import jakarta.persistence.*;
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
public class User extends AuditingCreation {

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
    private String encryptedPw;

    @Column(length = 255)
    private String thumbnailUrl;

    public User(String name) {
        this.name = name;
    }

    public User(String name, String loginId, String encryptedPw) {
        this.name = name;
        this.loginId = loginId;
        this.encryptedPw = encryptedPw;
    }

    public void changeEmail(String email) {
        this.email = email;
    }

    public void changeThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
