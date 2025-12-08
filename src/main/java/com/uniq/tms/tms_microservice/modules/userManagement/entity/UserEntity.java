package com.uniq.tms.tms_microservice.modules.userManagement.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.CalendarEntity;
import com.uniq.tms.tms_microservice.modules.organizationManagement.entity.RoleEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollAmountEntity;
import com.uniq.tms.tms_microservice.modules.payrollManagement.entity.UserPayRollEntity;
import com.uniq.tms.tms_microservice.modules.workScheduleManagement.entity.WorkScheduleEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 10)
    private String mobileNumber;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "organization_id")
    private String organizationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    @JsonBackReference
    private RoleEntity role;

    @Column(name = "is_default_password", nullable = false)
    private boolean isDefaultPassword = true;

    @Column(name = "date_of_joining")
    private LocalDate dateOfJoining;

    @Column(name = "is_register_user", nullable = false)
    private boolean isRegisterUser = false;

    @Column(name = "active", nullable = false)
    private boolean active;

    @ManyToOne
    @JoinColumn(name = "work_schedule_id")
    private WorkScheduleEntity workSchedule;

    @OneToMany(mappedBy = "user")
    private List<UserGroupEntity> userGroups;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPayRollAmountEntity> userPayrollAmounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserPayRollEntity> payrolls = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "calendar_id")
    private CalendarEntity calendar;

    @Column(name = "request_approver_id")
    private String approverId;

    public String getApproverId() {
        return approverId;
    }

    public void setApproverId(String approverId) {
        this.approverId = approverId;
    }

    public UserEntity(String userId){
        this.userId = userId;
    }

    public UserEntity() {
    }

    public UserEntity(String userId, String userName, String email, String mobileNumber, boolean isDefaultPassword, LocalDate dateOfJoining, boolean active,
                      RoleEntity role, LocalDateTime createdAt, String organizationId, String password, WorkScheduleEntity workSchedule) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.isDefaultPassword = isDefaultPassword;
        this.dateOfJoining = dateOfJoining;
        this.active = active;
        this.role = role;
        this.createdAt = createdAt;
        this.organizationId = organizationId;
        this.password = password;
        this.workSchedule = workSchedule;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }

    public boolean isDefaultPassword() {
        return isDefaultPassword;
    }

    public void setDefaultPassword(boolean defaultPassword) {
        isDefaultPassword = defaultPassword;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public RoleEntity getRole() {
        return role;
    }

    public void setRole(RoleEntity role) {
        this.role = role;
    }

    public boolean isRegisterUser() {
        return isRegisterUser;
    }

    public void setRegisterUser(boolean registerUser) {
        isRegisterUser = registerUser;
    }

    public WorkScheduleEntity getWorkSchedule() {
        return workSchedule;
    }

    public void setWorkSchedule(WorkScheduleEntity workSchedule) {
        this.workSchedule = workSchedule;
    }

    public List<UserGroupEntity> getUserGroups() {
        return userGroups;
    }

    public void setUserGroups(List<UserGroupEntity> userGroups) {
        this.userGroups = userGroups;
    }

    public List<UserPayRollEntity> getPayrolls() {
        return payrolls;
    }

    public void setPayrolls(List<UserPayRollEntity> payrolls) {
        this.payrolls = payrolls;
    }

    public CalendarEntity getCalendar() {
        return calendar;
    }

    public void setCalendar(CalendarEntity calendar) {
        this.calendar = calendar;
    }

    @Override
    public String toString(){
        return "UserEntity{ " + "userId='" + userId + '\'' +
                ", name='" + userName + '\''+
                '}';
    }
}
