package com.uniq.tms.tms_microservice.modules.timesheetManagement.adapter;

import com.uniq.tms.tms_microservice.modules.timesheetManagement.entity.UserFaceEntity;
import java.util.Optional;

public interface FaceAdapter {
    void saveUserFace(UserFaceEntity userFaceEntity);
    Optional<UserFaceEntity> findUserEmbeddingsById(String userId);
}
