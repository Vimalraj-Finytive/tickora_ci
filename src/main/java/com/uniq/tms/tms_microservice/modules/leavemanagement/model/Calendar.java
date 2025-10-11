package com.uniq.tms.tms_microservice.modules.leavemanagement.model;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ImportType;

public class Calendar {

    private String id;
    private String name;
    private Boolean isDefault;
    private ImportType importType;
    private String countryCode;
    private Boolean isActive;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public ImportType getImportType() {
        return importType;
    }

    public void setImportType(ImportType importType) {
        this.importType = importType;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
