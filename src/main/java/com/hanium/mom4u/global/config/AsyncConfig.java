package com.hanium.mom4u.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.concurrent.*;

@Configuration
@Slf4j
@EnableAsync
public class AsyncConfig {

    private static int CORE_POOL_SIZE = 30;
    private static int MAX_POOL_SIZE = 50;
    private static int QUEUE_CAPACITY = 200;
    private static int AWAIT_TERMINATION_SECONDS = 60;

    /**
     * 일반 Async용 스레드
     * @return
     */
    @Bean("asyncExecutor")
    public ThreadPoolTaskExecutor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);

        // Queue 대기열 가득 차서 ThreadPoolExecutor 직접 실행
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setThreadNamePrefix("Async-exec-");

        // 종료 시 대기 및 정리
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);

        executor.initialize();
        return executor;
    }

    /**
     * Scheduler 용 Async
     * @return
     */
    @Bean(name = "schedulerExecutor")
    public ThreadPoolTaskScheduler asyncSchedulerExecutor() {
        ThreadPoolTaskScheduler executor = new ThreadPoolTaskScheduler();
        executor.setPoolSize(CORE_POOL_SIZE);

        executor.setRemoveOnCancelPolicy(true);
        executor.setThreadNamePrefix("Async-sche-");

        // 예외 시
        executor.setErrorHandler(t -> {
            log.error("Async executor error", t);
                }
        );

        // 종료
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(AWAIT_TERMINATION_SECONDS);

        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

        executor.initialize();
        return executor;
    }
}
