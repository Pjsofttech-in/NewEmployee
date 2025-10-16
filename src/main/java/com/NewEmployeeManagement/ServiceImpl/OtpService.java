package com.NewEmployeeManagement.ServiceImpl;


import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_VALIDITY_DURATION = 5 * 60 * 1000; // 5 minutes
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();
    private final Random random = new Random();

    public String generateOtp(String email) {
        String otp = String.valueOf(100000 + random.nextInt(900000)); // 6-digit OTP
        long expiryTime = System.currentTimeMillis() + OTP_VALIDITY_DURATION;

        otpStorage.put(email, new OtpData(otp, expiryTime));
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        OtpData otpData = otpStorage.get(email);

        if (otpData == null || !otpData.getOtp().equals(otp) || System.currentTimeMillis() > otpData.getExpiryTime()) {
            return false;
        }

        otpStorage.remove(email); // OTP expires after one use
        return true;
    }

    private static class OtpData {
        private final String otp;
        private final long expiryTime;

        public OtpData(String otp, long expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public long getExpiryTime() {
            return expiryTime;
        }
    }
}