package com.uniq.tms.tms_microservice.modules.leavemanagement.services.impl;

import com.uniq.tms.tms_microservice.modules.leavemanagement.adapter.TimeoffPolicyAdapter;
import com.uniq.tms.tms_microservice.modules.leavemanagement.entity.TimeoffPolicyEntity;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.AccrualType;
import com.uniq.tms.tms_microservice.modules.leavemanagement.enums.Compensation;
import com.uniq.tms.tms_microservice.modules.leavemanagement.mapper.TimeoffPolicyEntityMapper;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.AccrualTypeEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.CompensationEnumModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.model.TimeoffPoliciesModel;
import com.uniq.tms.tms_microservice.modules.leavemanagement.services.TimeOffPolicyService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class TimeOffPolicyServiceImpl implements TimeOffPolicyService {
    private final TimeoffPolicyAdapter adapter;
    private final TimeoffPolicyEntityMapper mapper;

    public TimeOffPolicyServiceImpl(TimeoffPolicyAdapter adapter, TimeoffPolicyEntityMapper mapper) {
        this.adapter = adapter;
        this.mapper = mapper;
    }


    @Override
    public List<TimeoffPoliciesModel> getAllPolicies() {
        List<TimeoffPolicyEntity> entities = adapter.findAll();
        if (entities == null || entities.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No Data Found");
        }
        return entities.stream().map(entity -> {
            TimeoffPoliciesModel model = mapper.toModel(entity);
                    List<String> userIds = adapter.findUserIdsByPolicyId(entity.getPolicyId());
                    List<String> usernames = userIds.stream()
                            .map(adapter::findUsernameByUserId)
                            .toList();
                    model.setAssignedUsernames(usernames);
                    return model;
                })
                .toList();
    }

    @Override
    public List<TimeoffPoliciesModel> getAllPolicy() {
        List<TimeoffPolicyEntity> entities= adapter.findAll();
        if (entities==null||entities.isEmpty())throw new  ResponseStatusException(HttpStatus.CONFLICT, "No Data Found");
        return entities.stream().map(mapper::toModel).toList();
    }

    @Override
    public List<AccrualTypeEnumModel> getAccrualTypeStatus() {
        List<AccrualTypeEnumModel> list = new ArrayList<>();
        for (AccrualType e : AccrualType.values()) {
            AccrualTypeEnumModel model = new AccrualTypeEnumModel(e.name(), e.getValue());
            list.add(model);
        }
        return list;
    }

    @Override
    public List<CompensationEnumModel> getCompensation() {
       List<CompensationEnumModel> list=new ArrayList<>();
       for(Compensation e:Compensation.values()){
           CompensationEnumModel model= new CompensationEnumModel(e.name(),e.getValue());
           list.add(model);
       }
       return list;
    }

    @Override
    public TimeoffPoliciesModel getPolicyById(String id) {
    TimeoffPolicyEntity entity=adapter.findById(id);

        if (entity==null) new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No time-off policy found for userId");
      return mapper.toModel(entity);
    }

    @Override
    public List<TimeoffPoliciesModel> getPolicyByUserId(String userId){
       List<TimeoffPolicyEntity> entity=adapter.findByUserId(userId);
        if (entity==null) new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No time-off policy found for userId");
        return mapper.toModelList(entity);
    }


}
