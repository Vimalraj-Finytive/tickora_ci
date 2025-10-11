package com.uniq.tms.tms_microservice.modules.leavemanagement.controller;

import com.uniq.tms.tms_microservice.modules.leavemanagement.constant.LeaveConstant;
import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CountryDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.facade.CountryFacade;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping(LeaveConstant.COUNTRY_URL)
public class CountryController {

    private final CountryFacade countryFacade;

    public CountryController(CountryFacade countryFacade) {
        this.countryFacade = countryFacade;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CountryDto>>> getAll(){
        return ResponseEntity.ok( countryFacade.getAll());
    }

}
