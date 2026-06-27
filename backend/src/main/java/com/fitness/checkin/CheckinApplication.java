package com.fitness.checkin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fitness.checkin.mapper")
public class CheckinApplication {
    public static void main(String[] args) {
        SpringApplication.run(CheckinApplication.class, args);
    }
}
