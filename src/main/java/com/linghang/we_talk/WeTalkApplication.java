package com.linghang.we_talk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.linghang.we_talk.mapper")
public class WeTalkApplication {
    public static void main(String[] args) {
        SpringApplication.run(WeTalkApplication.class, args);
    }
}
