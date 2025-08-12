package com.uniq.tms.tms_microservice.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniq.tms.tms_microservice.model.NettyfishConfig;
import com.uniq.tms.tms_microservice.model.OtpSendResponse;
import com.uniq.tms.tms_microservice.service.NettyfishService;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NettyfishServiceImpl implements NettyfishService {

    private final NettyfishConfig nettyfishConfig;

    public NettyfishServiceImpl(NettyfishConfig nettyfishConfig) {
        this.nettyfishConfig = nettyfishConfig;
    }

    @Override
    public String generateOtp() {
        int otp = (int) (Math.random() * 900000) + 100000;
        return String.valueOf(otp);
    }

    @Override
    public OtpSendResponse sendOtp(String mobile, String generatedOtp) {
        String nettyfishApiUrl ="https://sms.nettyfish.com/api/v2/SendSMS?SenderId="+nettyfishConfig.getSenderId()+"&TemplateId="+
                nettyfishConfig.getTemplateId()+"&Message=Dear customer, Your OTP for logging into auction portal  "+
                generatedOtp+" . Please do not share this OTP with anyone. This OTP is valid only for 5mins.U.N.I.Q TECHNO&PrincipleEntityId="+
                nettyfishConfig.getPrincipleEntityId()+"&ApiKey="+nettyfishConfig.getApiKey()+"&ClientId="+nettyfishConfig.getClientId()+
                "&MobileNumbers="+mobile+"&Is_Unicode=false";
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(nettyfishApiUrl, String.class);
        System.out.println("Nettyfish Response: " + response);
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response);

            int errorCode = root.path("ErrorCode").asInt();
            JsonNode dataNode = root.path("Data").isArray() && root.path("Data").size() > 0
                    ? root.path("Data").get(0)
                    : null;

            int messageErrorCode = dataNode != null ? dataNode.path("MessageErrorCode").asInt() : -1;
            String messageErrorDescription = dataNode != null ? dataNode.path("MessageErrorDescription").asText() : "";

            if (errorCode == 0 && messageErrorCode == 0) {
                return new OtpSendResponse(true, "OTP sent successfully");
            } else {
                return new OtpSendResponse(false, "Failed to send OTP: " + messageErrorDescription);
            }
        } catch (Exception e) {
            return new OtpSendResponse(false, "Error parsing Nettyfish response: " + e.getMessage());
        }
    }
}
