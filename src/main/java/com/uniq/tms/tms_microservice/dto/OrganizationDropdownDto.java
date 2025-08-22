package com.uniq.tms.tms_microservice.dto;

import java.util.List;
import java.util.Map;

public class OrganizationDropdownDto {

    private List<String> countries;
    private List<Map<String, Object>> sizeRanges;

    public OrganizationDropdownDto(List<String> countries, List<Map<String, Object>> sizeRanges) {
        this.countries = countries;
        this.sizeRanges = sizeRanges;
    }

    public List<String> getCountries() {
        return countries;
    }

    public void setCountries(List<String> countries) {
        this.countries = countries;
    }

    public List<Map<String, Object>> getSizeRanges() {
        return sizeRanges;
    }

    public void setSizeRanges(List<Map<String,Object>> sizeRanges) {
        this.sizeRanges = sizeRanges;
    }
}
