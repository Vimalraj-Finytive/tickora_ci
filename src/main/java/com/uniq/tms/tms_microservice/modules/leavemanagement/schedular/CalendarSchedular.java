package com.uniq.tms.tms_microservice.modules.leavemanagement.schedular;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.CalendarAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.CalendarService;
import com.uniq.tms.tms_microservice.modules.organizationManagement.adapter.OrganizationAdapter;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.OrganizationEntity;
import com.uniq.tms.tms_microservice.shared.util.TenantUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class CalendarSchedular {

    private final OrganizationAdapter organizationAdapter;
    private final CalendarAdapter calendarAdapter;
    private final CalendarService calendarService;

    public CalendarSchedular(OrganizationAdapter organizationAdapter,
                             CalendarAdapter calendarAdapter, CalendarService calendarService) {
        this.organizationAdapter = organizationAdapter;
        this.calendarAdapter = calendarAdapter;
        this.calendarService = calendarService;
    }

    @Scheduled(cron = "0 59 23 31 12 ?", zone = "Asia/Kolkata")
    public void autoUpdateCalendarHolidays() {

        List<OrganizationEntity> organizations = organizationAdapter.findAll();
        for (OrganizationEntity org : organizations) {
            try {
                TenantUtil.setCurrentTenant(org.getSchemaName());
                List<CalendarEntity> calendars = calendarAdapter.findAllCalendar();
                int nextYear = LocalDate.now().plusYears(2).getYear();

                calendarService.createHolidayForNextYear(calendars, nextYear);
            } catch (Exception e) {
                log.error("Error while creating holidays for organization {}", org.getOrgName(), e);
            } finally {
                TenantUtil.clearTenant();
            }
        }

        log.info("Calendar scheduler completed");
    }
}

