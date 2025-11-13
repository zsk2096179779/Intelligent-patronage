package com.example.train_back;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.train_back.mapper")
public class TrainBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrainBackApplication.class, args);
    }

}
