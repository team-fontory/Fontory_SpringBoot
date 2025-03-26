package org.fontory.fontorybe;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EnableAsync
@EnableJpaAuditing
@SpringBootApplication
public class FonToryBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FonToryBeApplication.class, args);
    }

    @Bean
    public ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            String activeProfiles = String.join(", ", environment.getActiveProfiles());
            log.info("FONTory Backend Application started with active profiles: {}", 
                    activeProfiles.isEmpty() ? "default" : activeProfiles);
        };
    }
}
