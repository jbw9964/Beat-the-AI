package org.app.config;

import jakarta.annotation.*;
import java.util.*;
import org.springframework.context.annotation.*;

@Configuration
public class TimeZoneConfig {

    private static final String TIME_ZONE = "Asia/Seoul";

    @PostConstruct
    public void setTimezone() {
        TimeZone.setDefault(TimeZone.getTimeZone(TIME_ZONE));
    }
}
