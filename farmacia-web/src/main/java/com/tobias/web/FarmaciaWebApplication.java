package com.tobias.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.tobias.web", "com.tobias"})
public class FarmaciaWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(FarmaciaWebApplication.class, args);
    }
}
