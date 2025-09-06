package com.uniq.tms.tms_microservice.enums;

import java.util.Arrays;

public enum OrganizationSizeRangeEnum {

    RANGE_0_50("0-50", 50),
    RANGE_51_100("51-100", 100),
    RANGE_101_500("101-500", 500),
    RANGE_501_1000("501-1000", 1000),
    RANGE_1001_5000("1001-5000", 5000);

    private final String displayValue;
    private final int maxCount;

    OrganizationSizeRangeEnum(String displayValue, int maxCount) {
        this.displayValue = displayValue;
        this.maxCount = maxCount;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public static OrganizationSizeRangeEnum fromDisplayValue(String displayValue){
        return Arrays.stream(values())
                .filter(r -> r.getDisplayValue().equals(displayValue))
                .findFirst()
                .orElseThrow(()-> new IllegalArgumentException("Invalid Range: "+ displayValue));
    }

    public static OrganizationSizeRangeEnum fromMaxCount(int maxCount){
        return Arrays.stream(values())
                .filter(m -> m.getMaxCount() == maxCount)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid Max Count: " + maxCount));
    }
}
