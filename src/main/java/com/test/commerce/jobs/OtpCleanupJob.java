package com.test.commerce.jobs;



import com.test.commerce.services.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OtpCleanupJob {

    private final OtpService otpService;

    @Scheduled(fixedRate = 60000)
    public void removeExpiredOtps() {
        otpService.cleanupExpiredOtps();
    }
}