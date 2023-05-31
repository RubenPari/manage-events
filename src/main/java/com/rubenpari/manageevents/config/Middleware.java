package com.rubenpari.manageevents.config;

import com.rubenpari.manageevents.middlewares.CheckAuthMiddle;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class Middleware implements WebMvcConfigurer {

    /**
     * Add the middleware to the application
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new CheckAuthMiddle());
    }
}
