package com.uniq.tms.tms_microservice.entity;

import com.uniq.tms.tms_microservice.enums.WorkScheduleTypeEnum;
import jakarta.persistence.*;

@Entity
@Table(name = "work_schedule_type")
public class WorkScheduleTypeEntity {

    @Id
    @Column(name = "type_id", nullable = false, length = 20)
    private String typeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true)
    private WorkScheduleTypeEnum type;

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }

    public WorkScheduleTypeEnum getType() {
        return type;
    }

    public void setType(WorkScheduleTypeEnum type) {
        this.type = type;
    }
}

