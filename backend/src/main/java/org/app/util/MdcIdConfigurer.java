package org.app.util;

import jakarta.servlet.*;
import java.util.*;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.core.task.*;
import org.springframework.stereotype.*;

@Component
public class MdcIdConfigurer implements ServletRequestListener, TaskDecorator {

    private static final int ID_LENGTH = 10;
    private final String mdcKeyForRequestId;
    private final String mdcKeyForThreadId;

    public MdcIdConfigurer(
            @Value("${mdc-key.request-id}")
            String mdcKeyForRequestId,
            @Value("${mdc-key.thread-id}")
            String mdcKeyForThreadId
    ) {
        this.mdcKeyForRequestId = mdcKeyForRequestId;
        this.mdcKeyForThreadId = mdcKeyForThreadId;
    }

    @Override
    public void requestInitialized(ServletRequestEvent sre) {
        String newRequestId = genRandomId();
        MDC.put(mdcKeyForRequestId, newRequestId);
    }

    @Override
    public void requestDestroyed(ServletRequestEvent sre) {
        MDC.remove(mdcKeyForRequestId);
    }

    @Override
    public Runnable decorate(Runnable runnable) {
        String newThreadId = genRandomId();

        return () -> {
            try {
                MDC.put(mdcKeyForThreadId, newThreadId);
                runnable.run();
            } finally {
                MDC.remove(mdcKeyForThreadId);
            }
        };
    }

    private String genRandomId() {
        String uuid = UUID.randomUUID().toString()
                .replaceAll("-", "")
                .substring(0, ID_LENGTH);

        return String.format(
                "%s-%s",
                uuid.substring(0, ID_LENGTH / 2),
                uuid.substring(ID_LENGTH / 2)
        );
    }

    public String currentRequestId() {
        return MDC.get(mdcKeyForRequestId);
    }

    public String getCurrentThreadId() {
        return MDC.get(mdcKeyForThreadId);
    }
}
