package com.uniq.tms.tms_microservice.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // Serve static Angular files from /static/tmsweb/
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                        "/**",
                        "/assets/**",
                        "/assets/images/**",
                        "/favicon.ico",
                        "/index.html",
                        "/*.js",
                        "/*.css",
                        "/*.txt",
                        "/browser/_redirects"
                )
                .addResourceLocations(
                        "classpath:/static/browser/",
                        "classpath:/static/browser/assets/",
                        "classpath:/static/browser/assets/images/"
                );
    }


    // Optional: Forward any non-API path to index.html for Angular routing support
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{spring:[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/**/{spring:[a-zA-Z0-9-_]+}")
                .setViewName("forward:/index.html");
        registry.addViewController("/{spring:[a-zA-Z0-9-_]+}/**{spring:?!(\\.js|\\.css|\\.png)$}")
                .setViewName("forward:/index.html");
    }

}
