package com.uniq.tms.tms_microservice.modules.authenticationManagement.services;

import com.uniq.tms.tms_microservice.modules.authenticationManagement.model.OtpSendResponse;

public interface NettyfishService {
    String generateOtp();
    OtpSendResponse sendOtp(String mobile, String generatedOtp);
}
