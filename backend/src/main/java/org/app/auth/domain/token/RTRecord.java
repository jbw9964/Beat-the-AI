package org.app.auth.domain.token;

import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.redis.core.*;

@Getter
@RedisHash(value = "rt-registry", timeToLive = 864000)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RTRecord {

    @Id
    private Long userId;
    private String token;

    public RTRecord(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }
}
