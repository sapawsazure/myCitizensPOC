package com.poc.hub;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        // Same image as docker-compose.yml: bookworm keeps legacy time zone names (e.g. Asia/Calcutta) valid.
        return new PostgreSQLContainer(DockerImageName.parse("postgres:17-bookworm"));
    }
}
