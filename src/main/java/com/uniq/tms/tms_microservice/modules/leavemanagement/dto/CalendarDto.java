package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.ImportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CalendarDto {

    private String id;
    @NotNull(message = "Calendar name cannot be null")
    @NotBlank(message = "Calendar name is required")
    private String name;
    private Boolean isDefault;
    private ImportType importType;
    @NotBlank(message = "Country code is required")
    private String countryCode;
    private Boolean isActive;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public ImportType getImportType() {
        return importType;
    }

    public void setImportType(ImportType importType) {
        this.importType = importType;
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
