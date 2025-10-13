package com.uniq.tms.tms_microservice.modules.leavemanagement.services;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.Country;
import java.util.List;

public interface CountryService {
    List<Country> getAll();
}
