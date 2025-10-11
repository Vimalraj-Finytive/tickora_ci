package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "country", schema = "public")
public class CountryEntity {
    @Id
    private String id;
    @Column(name = "code", nullable = false)
    private String code;
    @Column(name = "name", nullable = false)
    private String name;
    @OneToMany(mappedBy = "country", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PublicHolidayEntity> publicHolidays;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PublicHolidayEntity> getPublicHolidays() {
        return publicHolidays;
    }

    public void setPublicHolidays(List<PublicHolidayEntity> publicHolidays) {
        this.publicHolidays = publicHolidays;
    }
}
