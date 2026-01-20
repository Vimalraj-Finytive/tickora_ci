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
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/browser/assets/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/*.*")
                .addResourceLocations("classpath:/static/browser/")
                .setCachePeriod(3600);
        registry.addResourceHandler("/")
                .addResourceLocations("classpath:/static/browser/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:(?!assets|tms|swagger-ui|v3)[^\\.]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{path:(?!assets|tms|swagger-ui|v3)[^\\.]+}")
                .setViewName("forward:/index.html");
        registry.addRedirectViewController("/","/login");
        registry.addRedirectViewController("","/login");
    }
}
