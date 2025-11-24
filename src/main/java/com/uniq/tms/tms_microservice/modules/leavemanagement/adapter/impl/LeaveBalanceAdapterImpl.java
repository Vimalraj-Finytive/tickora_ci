package com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeOffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.repository.LeaveBalanceRepository;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LeaveBalanceAdapterImpl implements LeaveBalanceAdapter {
    private final LeaveBalanceRepository leaveBalanceRepo;
    private final UserGroupRepository userGroupRepository;

    public LeaveBalanceAdapterImpl(LeaveBalanceRepository leaveBalanceRepo, UserGroupRepository userGroupRepository) {
        this.leaveBalanceRepo = leaveBalanceRepo;
        this.userGroupRepository = userGroupRepository;
    }

    @Override
    public void saveLeaveBalances(List<LeaveBalanceEntity> leaveBalances) {
        leaveBalanceRepo.saveAll(leaveBalances);
    }


    @Override
    public void deleteLeaveBalances(String policyId) {
        leaveBalanceRepo.deleteByPolicyId(policyId);
    }

    @Override
    public LeaveBalanceEntity findLeaveBalance(String userId) {
        return leaveBalanceRepo
                .findTopByUser_UserIdOrderByLeaveBalanceIdDesc(userId)
                .orElse(null);
    }

    @Override
    public List<LeaveBalanceEntity> findLeaveBalancesByPolicyId(String policyId) {
        return leaveBalanceRepo.findByPolicyId(policyId);
    }

    @Override
    public List<TimeOffPolicyEntity> findByUserId(String userId) {
        return List.of();
    }


    public List<LeaveBalanceEntity> findBalance(String userId) {
        return leaveBalanceRepo.findLeaveBalanceByUserId( userId);
    }

    public List<GroupEntity> getSupervisorGroups(String supervisorId) {
        return userGroupRepository.findGroupsWhereUserIsSupervisor(supervisorId);
    }

    public List<UserEntity> getMembers(Long groupId, String supervisorId) {
        return userGroupRepository.findMembersExcludingSupervisor(groupId, supervisorId);
    }

    @Override
    public List<LeaveBalanceEntity> getLeaveBalance(List<String> userIds) {
        return leaveBalanceRepo.findLeaveBalanceByUserIds((userIds));
    }
}
