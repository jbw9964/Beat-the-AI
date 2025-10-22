package org.app.util;

import java.time.*;
import java.util.*;

public class DateTimeProvider {

    public static LocalDateTime localDateTimeNow() {
        return LocalDateTime.now();
    }

    public static Date dateNow() {
        return Date.from(Instant.now());
    }

    public static Date dateNowAfter(long sec) {
        return Date.from(Instant.now().plusSeconds(sec));
    }
}
