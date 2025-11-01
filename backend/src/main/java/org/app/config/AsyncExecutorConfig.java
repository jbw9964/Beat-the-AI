package org.app.config;

import java.util.concurrent.*;
import lombok.*;
import org.app.util.*;
import org.springframework.context.annotation.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.scheduling.concurrent.*;

@EnableAsync
@Configuration
@RequiredArgsConstructor
class AsyncExecutorConfig {

    private static final boolean USE_VIRTUAL_THREAD = true;
    private static final int CORE_POOL_SIZE = 10;
    private static final int MAXIMUM_POOL_SIZE = 30;
    private static final int QUEUE_CAPACITY = 50;
    private static final String THREAD_NAME_PREFIX = "async-executor-";

    private final MdcIdConfigurer mdcIdConfigurer;

    @Bean(name = "taskExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setVirtualThreads(USE_VIRTUAL_THREAD);
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAXIMUM_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setTaskDecorator(mdcIdConfigurer);
        executor.initialize();
        return executor;
    }
}
