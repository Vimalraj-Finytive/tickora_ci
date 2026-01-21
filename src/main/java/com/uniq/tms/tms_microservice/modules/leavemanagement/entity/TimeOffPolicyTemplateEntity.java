package com.uniq.tms.tms_microservice.modules.leavemanagement.entity;

import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.GenderApplicability;
import jakarta.persistence.*;

@Entity
@Table(name = "timeoff_policy_templates")
public class TimeOffPolicyTemplateEntity {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "policy_code", length = 10)
    private String policyCode;

    @Column(name = "policy_name", nullable = false, length = 100)
    private String policyName;

    @Column(name = "entitled_units", nullable = false)
    private Integer entitledUnits;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender_applicability", length = 10)
    private GenderApplicability genderApplicability;

    public TimeOffPolicyTemplateEntity(String policyCode, String policyName, Integer entitledUnits, GenderApplicability genderApplicability) {
        this.policyCode = policyCode;
        this.policyName = policyName;
        this.entitledUnits = entitledUnits;
        this.genderApplicability = genderApplicability;
    }

    public TimeOffPolicyTemplateEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPolicyCode() {
        return policyCode;
    }

    public void setPolicyCode(String policyCode) {
        this.policyCode = policyCode;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Integer getEntitledUnits() {
        return entitledUnits;
    }

    public void setEntitledUnits(Integer entitledUnits) {
        this.entitledUnits = entitledUnits;
    }

    public GenderApplicability getGenderApplicability() {
        return genderApplicability;
    }

    public void setGenderApplicability(GenderApplicability genderApplicability) {
        this.genderApplicability = genderApplicability;
    }

}

