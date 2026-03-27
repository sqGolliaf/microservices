package ru.sg.notification.config

import org.springframework.boot.task.ThreadPoolTaskExecutorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import java.util.concurrent.Executor

@EnableAsync
@Configuration
class AsyncConfig {

    @Bean(name = ["emailExecutor"])
    fun emailExecutor(): Executor {
        val executor = ThreadPoolTaskExecutorBuilder()
            .corePoolSize(2)
            .maxPoolSize(5)
            .queueCapacity(100)
            .threadNamePrefix("email-sender-")
            .build()

        executor.initialize()

        return executor
    }
}