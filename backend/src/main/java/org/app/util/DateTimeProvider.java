package org.app.util;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.*;

@Component
public class DateTimeProvider {

    public LocalDateTime localDateTimeNow() {
        return LocalDateTime.now();
    }

    public Date dateNow() {
        return Date.from(Instant.now());
    }

    public Date secAfterFromDate(Date from, long sec) {
        return Date.from(from.toInstant().plusSeconds(sec));
    }
}
