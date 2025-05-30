package com.uniq.tms.tms_microservice.scheduler;

import com.uniq.tms.tms_microservice.service.TimesheetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class TimesheetScheduler {

    private static final Logger log = LoggerFactory.getLogger(TimesheetScheduler.class);

    private final TimesheetService timesheetService;

    public TimesheetScheduler(TimesheetService timesheetService) {
        this.timesheetService = timesheetService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void autoClockOutForAllEmployees() {
        try{
            log.info("Scheduled clock triggered at {}");
            timesheetService.autoClockOut();
        } catch (Exception e) {
            log.error("Error while auto clock out", e);
        }
    }
}
