package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.CountryAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.CountryEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Country;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.CountryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CountryServiceImpl implements CountryService {

    private static final Logger log = LoggerFactory.getLogger(CountryServiceImpl.class);

    private final CountryAdapter countryAdapter;
    private final CountryEntityMapper countryEntityMapper;

    public CountryServiceImpl(CountryAdapter countryAdapter, CountryEntityMapper countryEntityMapper) {
        this.countryAdapter = countryAdapter;
        this.countryEntityMapper = countryEntityMapper;
    }

    @Override
    public List<Country> getAll() {
        log.info("Fetching all countries...");
        List<Country> countries = countryAdapter.getAllCountries()
                .stream()
                .map(countryEntityMapper::toModel)
                .toList();
        log.info("Fetched {} countries", countries.size());
        return countries;
    }
}
