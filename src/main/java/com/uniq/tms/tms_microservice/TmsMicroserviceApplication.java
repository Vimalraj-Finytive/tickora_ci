package com.uniq.tms.tms_microservice;

import com.uniq.tms.tms_microservice.config.CacheDependencyConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAsync
@EnableConfigurationProperties(CacheDependencyConfig.class)
public class TmsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TmsMicroserviceApplication.class, args);
	}

}
