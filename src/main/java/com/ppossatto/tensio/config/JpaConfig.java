package com.ppossatto.tensio.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/// Enables JPA auditing, which fills `createdAt` / `updatedAt` of every {@link com.ppossatto.tensio.entity.common.BaseEntity}.
@Configuration
@EnableJpaAuditing
class JpaConfig {
}
