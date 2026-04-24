package com.bali.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DatabaseMigrationConfig {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMigrationConfig.class);

    @Bean
    public ApplicationRunner relaxPostsVillageConstraint(JdbcTemplate jdbcTemplate) {
        return args -> {
            try {
                jdbcTemplate.execute("ALTER TABLE IF EXISTS posts ALTER COLUMN village_group_id DROP NOT NULL");
                logger.info("Applied DB migration: posts.village_group_id is now nullable");
            } catch (Exception e) {
                logger.warn("Could not apply posts.village_group_id nullable migration: {}", e.getMessage());
            }
        };
    }
}
