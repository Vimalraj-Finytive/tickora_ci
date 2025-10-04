package com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.impl;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter.FaceAdapter;
import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.UserFaceEntity;
import com.uniq.tms.tms_microservice.modules.userManagement.repository.UserFaceRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class FaceAdapterImpl implements FaceAdapter {

    private final UserFaceRepository userFaceRepository;

    public FaceAdapterImpl(UserFaceRepository userFaceRepository) {
        this.userFaceRepository = userFaceRepository;
    }

    @Override
    public void saveUserFace(UserFaceEntity userFaceEntity) {
        userFaceRepository.save(userFaceEntity);
    }

    @Override
    public Optional<UserFaceEntity> findUserEmbeddingsById(String userId) {
        return userFaceRepository.findByUserId(userId);
    }
}
