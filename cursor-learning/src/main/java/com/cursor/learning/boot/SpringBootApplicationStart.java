package com.cursor.learning.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.cursor.learning")
@EntityScan("com.cursor.learning.domain.entity")
public class SpringBootApplicationStart {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootApplicationStart.class, args);
    }
} 