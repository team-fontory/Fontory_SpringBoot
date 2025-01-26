package org.fontory.fontorybe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class FonToryBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(FonToryBeApplication.class, args);
    }

}
