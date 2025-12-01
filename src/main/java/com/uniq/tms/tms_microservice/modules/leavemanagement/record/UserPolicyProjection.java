package com.uniq.tms.tms_microservice.modules.leavemanagement.record;

import java.time.LocalDate;

public record UserPolicyProjection(UserPolicyKey key, LocalDate validTo) {
}
