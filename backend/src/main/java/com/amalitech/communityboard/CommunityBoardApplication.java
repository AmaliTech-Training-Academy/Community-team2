package com.amalitech.communityboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class CommunityBoardApplication {
    public static void main(String[] args) {
        SpringApplication.run(CommunityBoardApplication.class, args);
    }
}
