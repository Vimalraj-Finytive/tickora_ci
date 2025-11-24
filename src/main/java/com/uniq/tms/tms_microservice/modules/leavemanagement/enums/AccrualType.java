package com.uniq.tms.tms_microservice.modules.leavemanagement.enums;

public enum AccrualType {

        MONTHLY("Monthly"),
        ANNUALLY("Annually"),
        FIXED("Fixed");

        private final String value;

        AccrualType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }


