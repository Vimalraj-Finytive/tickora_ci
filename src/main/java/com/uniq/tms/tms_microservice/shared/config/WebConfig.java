package com.uniq.tms.tms_microservice.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/");
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler(
                        "/*.js",
                        "/*.css",
                        "/*.ico",
                        "/*.txt",
                        "/*.json"
                ).addResourceLocations("classpath:/static/browser/")
                .setCachePeriod(0);
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/browser/assets/")
                .setCachePeriod(0);
        registry.addResourceHandler("/", "/index.html")
                .addResourceLocations("classpath:/static/browser/")
                .setCachePeriod(0);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{spring:(?!assets|tms|swagger-ui|v3).*}/**")
                .setViewName("forward:/index.html");
    }
}
