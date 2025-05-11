package com.cansuiremkanli.libmanage.scheduler;

import com.cansuiremkanli.libmanage.service.BorrowingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class BorrowingSchedulerTest {

    private BorrowingScheduler borrowingScheduler;
    private BorrowingService borrowingService;

    @BeforeEach
    void setUp() {
        borrowingService = mock(BorrowingService.class);
        borrowingScheduler = new BorrowingScheduler(borrowingService);
    }

    @Test
    void testCheckOverdues_callsUpdateOverdueStatuses() {
        borrowingScheduler.checkOverdues();

        verify(borrowingService, times(1)).updateOverdueStatuses();
    }
}
