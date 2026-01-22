package com.uniq.tms.tms_microservice.modules.organizationManagement.controller;

import com.uniq.tms.tms_microservice.modules.organizationManagement.constant.OrganizationConstant;
import com.uniq.tms.tms_microservice.modules.organizationManagement.facade.OrganizationFacade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(OrganizationConstant.WEBHOOK_URL)
public class RazorpayWebhookController {

    private final OrganizationFacade organizationFacade;

    public RazorpayWebhookController(OrganizationFacade organizationFacade) {
        this.organizationFacade = organizationFacade;
    }

    @PostMapping("/receive")
    public ResponseEntity<String> receiveWebhook(@RequestBody String payload,
                                                 @RequestHeader("X-Razorpay-Signature") String signature){
        return organizationFacade.handleWebhook(payload, signature);

    }
}

