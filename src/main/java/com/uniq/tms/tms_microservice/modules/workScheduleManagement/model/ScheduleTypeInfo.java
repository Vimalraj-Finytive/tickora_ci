package com.uniq.tms.tms_microservice.modules.workScheduleManagement.model;

import com.uniq.tms.tms_microservice.modules.workScheduleManagement.enums.WorkScheduleTypeEnum;
import java.time.Duration;
import java.time.LocalTime;

public class ScheduleTypeInfo {

        private  WorkScheduleTypeEnum type;
        private  LocalTime startTime;
        private  LocalTime endTime;
        private  Duration duration;

        public static ScheduleTypeInfo fixed(LocalTime startTime, LocalTime endTime) {
            return new ScheduleTypeInfo(WorkScheduleTypeEnum.FIXED, startTime, endTime, Duration.between(startTime, endTime));
        }

        public static ScheduleTypeInfo flexible(Duration duration) {
            return new ScheduleTypeInfo(WorkScheduleTypeEnum.FLEXIBLE, null, null, duration);
        }

        private ScheduleTypeInfo(WorkScheduleTypeEnum type, LocalTime startTime, LocalTime endTime, Duration duration) {
            this.type = type;
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = duration;
        }

        public boolean isFixed() {
            return type == WorkScheduleTypeEnum.FIXED;
        }

        public boolean isFlexible() {
            return type == WorkScheduleTypeEnum.FLEXIBLE;
        }

        public WorkScheduleTypeEnum getType() { return type; }
        public LocalTime getStartTime() { return startTime; }
        public LocalTime getEndTime() { return endTime; }
        public Duration getDuration() { return duration; }
}
