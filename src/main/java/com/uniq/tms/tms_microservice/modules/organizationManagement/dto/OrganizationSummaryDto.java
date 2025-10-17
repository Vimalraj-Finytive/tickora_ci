package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class OrganizationSummaryDto {
    private String organizationId;
    private String name;
    private String type;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate createdAt;
    private Counts counts;
    private String country;
    private Integer orgSize;

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getOrgSize() {
        return orgSize;
    }

    public void setOrgSize(Integer orgSize) {
        this.orgSize = orgSize;
    }

    public static class Counts {
        private int totalMembers;
        private int activeMembers;
        private int inactiveMembers;
        private int subscriptionUserCount;
        private int addedUsers;
        private int availableSubscriptionSlots;

        public int getTotalMembers() {
            return totalMembers;
        }

        public void setTotalMembers(int totalMembers) {
            this.totalMembers = totalMembers;
        }

        public int getActiveMembers() {
            return activeMembers;
        }

        public void setActiveMembers(int activeMembers) {
            this.activeMembers = activeMembers;
        }

        public int getInactiveMembers() {
            return inactiveMembers;
        }

        public void setInactiveMembers(int inactiveMembers) {
            this.inactiveMembers = inactiveMembers;
        }

        public int getSubscriptionUserCount() {
            return subscriptionUserCount;
        }

        public void setSubscriptionUserCount(int subscriptionUserCount) {
            this.subscriptionUserCount = subscriptionUserCount;
        }

        public int getAddedUsers() {
            return addedUsers;
        }

        public void setAddedUsers(int addedUsers) {
            this.addedUsers = addedUsers;
        }

        public int getAvailableSubscriptionSlots() {
            return availableSubscriptionSlots;
        }

        public void setAvailableSubscriptionSlots(int availableSubscriptionSlots) {
            this.availableSubscriptionSlots = availableSubscriptionSlots;
        }
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public Counts getCounts() {
        return counts;
    }

    public void setCounts(Counts counts) {
        this.counts = counts;
    }

    public OrganizationSummaryDto(String name, String type, LocalDate createdAt, String country, Integer orgSize, Counts counts) {
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
        this.country = country;
        this.orgSize = orgSize;
        this.counts = counts;
    }

    public OrganizationSummaryDto() {

    }



}
