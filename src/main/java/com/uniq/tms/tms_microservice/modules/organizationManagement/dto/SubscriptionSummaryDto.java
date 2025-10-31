package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import java.time.LocalDate;

public class SubscriptionSummaryDto {
    private String activePlan;
    private LocalDate startDate;
    private LocalDate endDate;
    private int subscribedUsers;
    private String planCycle;
    private String status;


    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Getters and Setters
    public String getActivePlan() { return activePlan; }
    public void setActivePlan(String activePlan) { this.activePlan = activePlan; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public int getSubscribedUsers() { return subscribedUsers; }
    public void setSubscribedUsers(int subscribedUsers) { this.subscribedUsers = subscribedUsers; }

    public String getPlanCycle() { return planCycle; }
    public void setPlanCycle(String planCycle) { this.planCycle = planCycle; }
}
