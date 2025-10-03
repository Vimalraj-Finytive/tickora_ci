package com.uniq.tms.tms_microservice.shared.security.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Component
public class OtpFallbackCache {

    private static final Logger log = LogManager.getLogger(OtpFallbackCache.class);

    private static final long OTP_TTL_DAYS = 90;

    private final Map<String, OtpEntry> otpMap = new ConcurrentHashMap<>();
    private final Map<String, CountEntry> otpCountMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    /** Save OTP for 90 days */
    public void saveOtp(String key, String otp) {
        long expireTime = Instant.now().plus(OTP_TTL_DAYS, ChronoUnit.DAYS).toEpochMilli();
        otpMap.put(key, new OtpEntry(otp, expireTime));
        scheduledExecutorService.schedule(() -> {
            OtpEntry otpEntry = otpMap.get(key);
            if (otpEntry != null && Instant.now().toEpochMilli() >= otpEntry.expireTime) {
                otpMap.remove(key);
                log.info("OTP expired and removed for key: {}", key);
            }
        }, OTP_TTL_DAYS, TimeUnit.DAYS);
    }

    /** Get OTP if still valid */
    public String getOtp(String key) {
        OtpEntry otpEntry = otpMap.get(key);
        if (otpEntry != null && System.currentTimeMillis() <= otpEntry.expireTime) {
            return otpEntry.otp;
        }
        otpMap.remove(key);
        return null;
    }

    /** Increment OTP request count, expires after 90 days */
    public void incrementCount(String key) {
        long expireTime = Instant.now().plus(OTP_TTL_DAYS, ChronoUnit.DAYS).toEpochMilli();
        otpCountMap.merge(key,
                new CountEntry(1, expireTime),
                (oldVal, newVal) -> {
                    if (Instant.now().toEpochMilli() > oldVal.expireTime) {
                        return newVal; // expired → reset count
                    }
                    oldVal.count++;
                    return oldVal;
                });
    }

    /** Get current OTP request count */
    public Integer getCount(String key) {
        CountEntry entry = otpCountMap.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expireTime) {
            otpCountMap.remove(key);
            return null;
        }
        return entry.count;
    }

    /** Manually clear OTP */
    public void clearOtp(String otpKey) {
        otpMap.remove(otpKey);
    }

    /** Debugging - get all OTPs */
    public Map<String, String> getAllEntries() {
        return otpMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().otp + " Expires: " + Instant.ofEpochMilli(e.getValue().expireTime)
                ));
    }

    /** Debugging - get all OTP counts */
    public Map<String, String> getAllOtpCounts() {
        return otpCountMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().count + " Expires: " +
                                Instant.ofEpochMilli(e.getValue().expireTime)
                ));
    }

    /** Count entry structure */
    private static class CountEntry {
        int count;
        long expireTime;

        CountEntry(int count, long expireTime) {
            this.count = count;
            this.expireTime = expireTime;
        }
    }

    /** OTP entry structure */
    private static class OtpEntry {
        String otp;
        long expireTime;

        OtpEntry(String otp, long expireTime) {
            this.otp = otp;
            this.expireTime = expireTime;
        }
    }
}
