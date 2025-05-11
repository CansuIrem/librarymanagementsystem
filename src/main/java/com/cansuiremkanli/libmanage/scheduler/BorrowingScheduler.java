package com.cansuiremkanli.libmanage.scheduler;

import com.cansuiremkanli.libmanage.service.BorrowingService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BorrowingScheduler {

    private final BorrowingService borrowingService;

    @Scheduled(cron = "0 0 * * * *") // Her saat başı
    public void checkOverdues() {
        borrowingService.updateOverdueStatuses();
    }
}
