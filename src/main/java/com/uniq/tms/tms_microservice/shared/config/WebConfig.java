package com.uniq.tms.tms_microservice.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        // Swagger UI
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui/");

        // OpenAPI JSON
        registry.addResourceHandler("/v3/api-docs/**")
                .addResourceLocations("classpath:/META-INF/resources/");

        // Angular static files
        registry.addResourceHandler(
                        "/",
                        "/index.html",
                        "/assets/**",
                        "/*.js",
                        "/*.css",
                        "/*.ico",
                        "/*.txt"
                ).addResourceLocations("classpath:/static/browser/")
                .setCachePeriod(0);
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {

        // Forward ONLY non-file, non-api routes
        registry.addViewController("/{path:[^\\.]*}")
                .setViewName("forward:/index.html");

        registry.addViewController("/**/{path:[^\\.]*}")
                .setViewName("forward:/index.html");
    }

}
