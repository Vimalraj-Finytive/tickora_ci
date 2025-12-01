package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import java.util.List;
import java.util.Map;

public class EntitledTypeDropdownDto {

    private List<Map<String, Object>> entitledTypes;

    public EntitledTypeDropdownDto(List<Map<String, Object>> entitledTypes) {
        this.entitledTypes = entitledTypes;
    }

    public List<Map<String, Object>> getEntitledTypes() {
        return entitledTypes;
    }

    public void setEntitledTypes(List<Map<String, Object>> entitledTypes) {
        this.entitledTypes = entitledTypes;
    }
}