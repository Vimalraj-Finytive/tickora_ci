package com.uniq.tms.tms_microservice.modules.leavemanagement.facade;

import com.uniq.tms.tms_microservice.modules.leavemanagement.dto.CountryDto;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.CountryDtoMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.CountryService;
import com.uniq.tms.tms_microservice.shared.dto.ApiResponse;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class CountryFacade {

    private final CountryService countryService;
    private final CountryDtoMapper countryDtoMapper;

    public CountryFacade(CountryService countryService, CountryDtoMapper countryDtoMapper) {
        this.countryService = countryService;
        this.countryDtoMapper = countryDtoMapper;
    }

    public ApiResponse<List<CountryDto>> getAll() {
        try {
            List<CountryDto> countryDtos = countryService.getAll().stream()
                    .map(countryDtoMapper::toDto)
                    .toList();
            return new ApiResponse<>(200,"Countries Fetched Successfully", countryDtos);
        } catch (Exception e) {
            return new ApiResponse(500, "Internal Server Error: " + e.getMessage(), null);
        }
    }

}
