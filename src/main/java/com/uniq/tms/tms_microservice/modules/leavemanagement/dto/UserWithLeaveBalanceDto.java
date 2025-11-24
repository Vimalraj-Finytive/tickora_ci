package com.uniq.tms.tms_microservice.modules.leavemanagement.dto;

import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;

import java.util.List;

public class UserWithLeaveBalanceDto {
    private String userId;
    private String userName;
    private String groupName;
    private List<LeaveBalanceModel> leaveBalances;

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

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<LeaveBalanceModel> getLeaveBalances() {
        return leaveBalances;
    }

    public void setLeaveBalances(List<LeaveBalanceModel> leaveBalances) {
        this.leaveBalances = leaveBalances;
    }

}

