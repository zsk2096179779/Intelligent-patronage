package com.fengqi.fund.fundadvisor;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.fengqi.fund.fundadvisor.mapper")
public class FundAdvisorApplication {

    public static void main(String[] args) {
        SpringApplication.run(FundAdvisorApplication.class, args);
    }

}
