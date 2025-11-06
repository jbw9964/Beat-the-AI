package org.app.auth.repository;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.redis.core.*;

@Getter
@RedisHash("rt-registry")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RTRecord {

    @Id
    private Long userId;
    private String token;

    @TimeToLive
    private Long expiration;

    public RTRecord(Long userId, String token, long expiration) {
        this.userId = userId;
        this.token = token;
        this.expiration = expiration;
    }
}
