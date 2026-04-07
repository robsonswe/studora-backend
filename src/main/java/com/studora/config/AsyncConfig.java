package com.studora.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("dbStatsExecutor")
    @Profile("!test")
    public Executor dbStatsExecutor() {
        ThreadPoolTaskExecutor exec = new ThreadPoolTaskExecutor();
        exec.setCorePoolSize(8);
        exec.setMaxPoolSize(16);
        exec.setQueueCapacity(50);
        exec.setThreadNamePrefix("db-stats-");
        exec.initialize();
        return exec;
    }

    @Bean("dbStatsExecutor")
    @Profile("test")
    public Executor dbStatsExecutorTest() {
        return new SyncTaskExecutor();
    }
}
