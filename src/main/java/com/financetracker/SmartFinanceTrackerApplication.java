package com.financetracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.financetracker")
@EntityScan(basePackages = "com.financetracker.entity")
@EnableJpaRepositories(basePackages = "com.financetracker.repository")
public class SmartFinanceTrackerApplication {
    public static void main(String[] args) {
        SpringApplication.run(SmartFinanceTrackerApplication.class, args);
    }
}
