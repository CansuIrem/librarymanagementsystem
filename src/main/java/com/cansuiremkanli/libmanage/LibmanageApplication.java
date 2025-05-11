package com.cansuiremkanli.libmanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class LibmanageApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibmanageApplication.class, args);
    }

}
