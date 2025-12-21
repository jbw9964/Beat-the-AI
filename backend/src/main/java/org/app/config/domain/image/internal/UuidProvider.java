package org.app.config.domain.image.internal;

import java.util.*;
import org.springframework.stereotype.*;

@Component
public class UuidProvider {

    public String getRandomUuidAsString() {
        return UUID.randomUUID().toString();
    }
}
