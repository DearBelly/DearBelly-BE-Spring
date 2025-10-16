package com.hanium.mom4u;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Mom4uApplication {

    public static void main(String[] args) {
        SpringApplication.run(Mom4uApplication.class, args);
    }

}
