package com.uniq.tms.tms_microservice.service;

import com.uniq.tms.tms_microservice.model.OtpSendResponse;

public interface NettyfishService {
    String generateOtp();
    OtpSendResponse sendOtp(String mobile, String generatedOtp);
}
