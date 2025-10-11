package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "calendar")
public class CalendarEntity {

    @Id
    private String id;

    private String name;

    private Boolean isDefault;

    private Boolean isActive;

    @OneToMany(mappedBy = "calendar", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CalendarHolidayEntity> calendarHolidays;

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

    public List<CalendarHolidayEntity> getCalendarHolidays() {
        return calendarHolidays;
    }

    public void setCalendarHolidays(List<CalendarHolidayEntity> calendarHolidays) {
        this.calendarHolidays = calendarHolidays;
    }

    public Boolean getDefault() {
        return isDefault;
    }

    public void setDefault(Boolean aDefault) {
        isDefault = aDefault;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }
}
