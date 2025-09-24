package com.finacial.wealth.api.profiling.config;

import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.beans.factory.annotation.Value;


//https://www.programmersought.com/article/27274780411/

@Configuration
@EnableAsync
public class AsyncConfiguration {
    @Value("${nxg.threadpool.queue-capacity}")
    private int threadpoolQueueCapacity;

    @Value("${nxg.threadpool.core-pool-size}")
    private int threadpoolCorePoolSize;

    @Value("${nxg.threadpool.max-pool-size}")
    private int threadpoolMaxPoolSize;

    @Bean("threadPoolTaskExecutor")
    public TaskExecutor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(threadpoolCorePoolSize);
        executor.setMaxPoolSize(threadpoolMaxPoolSize);
        executor.setQueueCapacity(threadpoolQueueCapacity);
        //executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardPolicy());
        executor.setThreadNamePrefix("Session-Manager-Service-Async-");
        return executor;
    }
}
