package com.uniq.tms.tms_microservice.shared.security.cache;

import com.uniq.tms.tms_microservice.modules.locationManagement.dto.LocationDto;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class CacheConfig {

    @Bean
    public Map<String, List<LocationDto>> locationCache() {
        return new ConcurrentHashMap<>();
    }
}
