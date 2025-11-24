package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.LeaveBalanceAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.LeaveBalanceEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeOffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.LeaveBalanceModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.UserWithLeaveBalanceModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.LeaveBalanceService;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.GroupEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.entity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LeaveBalanceServiceImpl implements LeaveBalanceService {
       private final LeaveBalanceAdapter leaveBalanceAdapter;
       private final TimeOffPolicyEntityMapper timeOffPolicyEntityMapper;

    public LeaveBalanceServiceImpl(LeaveBalanceAdapter leaveBalanceAdapter, TimeOffPolicyEntityMapper timeOffPolicyEntityMapper) {
        this.leaveBalanceAdapter = leaveBalanceAdapter;
        this.timeOffPolicyEntityMapper = timeOffPolicyEntityMapper;
    }

    @Override
    public List<LeaveBalanceModel> getLeaveBalance(String userId) {
        List<LeaveBalanceEntity> entities = leaveBalanceAdapter.findBalance(userId);
        return timeOffPolicyEntityMapper.toBalanceModelList(entities);
        }



    @Override
    public List<UserWithLeaveBalanceModel> getSupervisorLeave(String userId) {
        List<GroupEntity> groups = leaveBalanceAdapter.getSupervisorGroups(userId);
        if (groups.isEmpty()) {
            throw new RuntimeException("User is not a supervisor");
        }
        List<UserWithLeaveBalanceModel> result = new ArrayList<>();
        for (GroupEntity group : groups) {
            List<UserEntity> members = leaveBalanceAdapter.getMembers(group.getGroupId(), userId);
            if (members.isEmpty()) continue;

            List<String> memberIds = members.stream()
                    .map(UserEntity::getUserId)
                    .toList();

            List<LeaveBalanceEntity> balances =
                    leaveBalanceAdapter.getLeaveBalance(memberIds);
            Map<String, List<LeaveBalanceEntity>> groupedBalances =
                    balances.stream()
                            .collect(Collectors.groupingBy(lb -> lb.getUser().getUserId()));

            for (UserEntity member : members) {
                UserWithLeaveBalanceModel model = new UserWithLeaveBalanceModel();
                model.setUserId(member.getUserId());
                model.setUserName(member.getUserName());
                model.setGroupName(group.getGroupName());

                List<LeaveBalanceEntity> userBalanceList =
                        groupedBalances.getOrDefault(member.getUserId(), List.of());

                model.setLeaveBalances(
                        timeOffPolicyEntityMapper.toBalanceModelList(userBalanceList)
                );

                result.add(model);
            }
        }

        return result;
    }

}
