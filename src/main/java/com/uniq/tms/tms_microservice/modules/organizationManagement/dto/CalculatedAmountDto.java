package com.uniq.tms.tms_microservice.modules.organizationManagement.dto;

public class CalculatedAmountDto {
    private double amount;

    public CalculatedAmountDto(double amount) {
        this.amount = amount;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
