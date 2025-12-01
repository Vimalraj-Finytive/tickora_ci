package com.uniq.tms.tms_microservice.modules.payrollManagement.entity;

import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import jakarta.persistence.*;

@Entity
@Table(name = "user_payroll")
public class UserPayRollEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payroll_id", nullable = false)
    private PayRollEntity payroll;

    public UserPayRollEntity(){}

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public PayRollEntity getPayroll() {
        return payroll;
    }

    public void setPayroll(PayRollEntity payroll){
        this.payroll = payroll;
    }


}
