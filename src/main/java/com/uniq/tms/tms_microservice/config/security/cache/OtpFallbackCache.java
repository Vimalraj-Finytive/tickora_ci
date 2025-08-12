package com.uniq.tms.tms_microservice.config.security.cache;

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

    private final Map<String, OtpEntry> otpMap = new ConcurrentHashMap<>();
    private final Map<String, CountEntry> otpCountMap = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    public void saveOtp(String key, String otp, long ttlMinutes){
        otpMap.put(key, new OtpEntry(otp, Instant.now().plus(ttlMinutes, ChronoUnit.MINUTES).toEpochMilli()));
        scheduledExecutorService.schedule(()-> {
            OtpEntry otpEntry = otpMap.get(key);
            if(otpEntry != null && Instant.now().toEpochMilli() >= otpEntry.expireTime){
                otpMap.remove(key);
                log.info("OTP expired and removed for key: {}" , key);
            }
        },ttlMinutes, TimeUnit.MINUTES);
    }

    public String getOtp(String key) {
        OtpEntry otpEntry = otpMap.get(key);
        if (otpEntry != null && System.currentTimeMillis() <= otpEntry.expireTime) {
            return otpEntry.otp;
        }
        otpMap.remove(key);
        return null;
    }

    public void incrementCount(String key, long secondsUntilMidnight) {
        otpCountMap.merge(key,
                new CountEntry(1, Instant.now().plus(secondsUntilMidnight, ChronoUnit.SECONDS).toEpochMilli()),
                (oldVal, newVal) -> {
            if (Instant.now().toEpochMilli() > oldVal.expireTime) {
                return newVal;
            }
            oldVal.count++;
            return oldVal;
        });
    }

    public Integer getCount(String key) {
        CountEntry entry = otpCountMap.get(key);
        if (entry == null) return null;
        if (System.currentTimeMillis() > entry.expireTime) {
            otpCountMap.remove(key);
            return null;
        }
        return entry.count;
    }

    public void clearOtp(String otpKey) {
        otpMap.remove(otpKey);
    }

    public Map<String, String> getAllEntries() {
        return otpMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e-> e.getValue().otp + " Expires: " + Instant.ofEpochMilli(e.getValue().expireTime) + ")"
                ));
    }

    public Map<String, String> getAllOtpCounts() {
        return otpCountMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().count + " Expires: " +
                                Instant.ofEpochMilli(e.getValue().expireTime)
                ));
    }

    private static class CountEntry {
            int count;
            long expireTime;
            CountEntry(int count, long expireTime) {
                this.count = count;
                this.expireTime = expireTime;
            }
        }

    private static class OtpEntry{
        String otp;
        long expireTime;

        OtpEntry(String otp, long expireTime){
            this.otp = otp;
            this.expireTime = expireTime;
        }
    }

}
