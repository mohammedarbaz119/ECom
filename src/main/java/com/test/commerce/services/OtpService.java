package com.test.commerce.services;


import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

@Service
public class OtpService {

    private static class OtpEntry {
        String otp;
        LocalDateTime expiryTime;

        OtpEntry(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiryTime);
        }
    }

    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();
    private final int OTP_EXPIRATION_MINUTES = 5;

    public String generateOtp(String key) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRATION_MINUTES);
        otpStore.put(key, new OtpEntry(otp, expiry));
        return otp;
    }

    public boolean validateOtp(String key, String otp) {
        OtpEntry entry = otpStore.get(key);
        if (entry == null || entry.isExpired()) {
            otpStore.remove(key);
            return false;
        }
        boolean valid = entry.otp.equals(otp);
        if (valid) otpStore.remove(key);
        return valid;
    }

    public void cleanupExpiredOtps() {
        otpStore.entrySet().removeIf(e -> e.getValue().isExpired());
    }
}