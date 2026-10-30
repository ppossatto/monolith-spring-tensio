package com.ppossatto.tensio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/// Enables `@Async` methods (e.g. sending e-mails) on the task executor auto-configured by Spring Boot.
@Configuration
@EnableAsync
class AsyncConfig {
}
