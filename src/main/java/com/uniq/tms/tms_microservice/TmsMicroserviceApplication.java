package com.uniq.tms.tms_microservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TmsMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(TmsMicroserviceApplication.class, args);
	}

}
