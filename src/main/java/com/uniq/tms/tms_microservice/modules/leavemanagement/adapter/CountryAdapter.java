package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter;

import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CountryEntity;
import java.util.List;

public interface CountryAdapter {
    List<CountryEntity> getAllCountries();
}
