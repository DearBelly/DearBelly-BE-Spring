package com.hanium.mom4u;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableJpaAuditing
public class Mom4uApplication {

    public static void main(String[] args) {
        SpringApplication.run(Mom4uApplication.class, args);
    }

}
