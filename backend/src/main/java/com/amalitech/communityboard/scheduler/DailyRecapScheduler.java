package com.amalitech.communityboard.scheduler;

import com.amalitech.communityboard.service.interfaces.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyRecapScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 41 8 * * *")
    public void sendDailyRecap() {
        System.out.println("about to start daily recap");
        LocalDate targetDate = LocalDate.now().minusDays(1);
        notificationService.sendDailyRecapForDate(targetDate);
    }
}

