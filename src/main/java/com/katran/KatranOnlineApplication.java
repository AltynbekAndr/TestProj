package com.katran;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.katran.repository")
@EnableGlobalMethodSecurity(prePostEnabled = true, proxyTargetClass = true)
public class KatranOnlineApplication {

    public static void main(String[] args) {
        SpringApplication.run(KatranOnlineApplication.class, args);
    }

}
























