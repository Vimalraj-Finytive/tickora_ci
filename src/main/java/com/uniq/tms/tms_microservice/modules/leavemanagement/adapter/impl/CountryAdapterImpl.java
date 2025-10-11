package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.CountryAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CountryEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.CountryRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@ConditionalOnProperty(name = "database.type", havingValue = "postgres")
public class CountryAdapterImpl implements CountryAdapter {

    private final CountryRepository countryRepository;

    public CountryAdapterImpl(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    @Override
    public List<CountryEntity> getAllCountries() {
        return countryRepository.findAll();
    }

}
