package com.uniq.tms.tms_microservice.dto;

public enum TimesheetStatusEnum {

    PRESENT(1L, "Present"),
    ABSENT(2L, "Absent"),
    PAID_LEAVE(3L, "Paid Leave"),
    NOT_MARKED(4L, "Not Marked"),
    HOLIDAY(5L, "Holiday");

    private final Long id;
    private final String label;

    TimesheetStatusEnum(Long id, String label) { this.id = id; this.label = label; }
    public Long getId() { return id; }

    public String getLabel() {
        return label;
    }

    public static TimesheetStatusEnum getStatusFromId(Long id) {
        for(TimesheetStatusEnum status : TimesheetStatusEnum.values()) {
            if (status.getId().equals(id)) {
                return status;
            }
        }
        return null;
    }
}
