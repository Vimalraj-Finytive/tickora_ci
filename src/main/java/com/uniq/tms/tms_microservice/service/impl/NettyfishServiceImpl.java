package com.uniq.tms.tms_microservice.service.impl;

import com.uniq.tms.tms_microservice.service.NettyfishService;
import org.springframework.stereotype.Service;

@Service
public class NettyfishServiceImpl implements NettyfishService {
    @Override
    public String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }
}
