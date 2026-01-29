package com.uniq.tms.tms_microservice.modules.ReportManagement.helper;

import com.uniq.tms.tms_microservice.shared.util.ExportStatusTracker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import java.io.File;

@Component
public class ExportStatusResolver {

    public String resolve(
            String redisKey,
            File file,
            RedisTemplate<String,Object> redisTemplate,
            ExportStatusTracker exportStatusTracker
    ){
        if (redisTemplate != null){
            Object status = redisTemplate.opsForValue().get(redisKey);
            if (status != null){
                return status.toString();
            }
        }

        String fileStatus = exportStatusTracker.readStatus(file);
        return  fileStatus!=null ? fileStatus : "NOT_FOUND";
    }
}
