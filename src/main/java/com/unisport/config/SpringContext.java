package com.unisport.config;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class SpringContext implements ApplicationContextAware {

    private static ApplicationContext CTX;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        CTX = applicationContext;
    }

    public static String getProperty(String key) {
        if (CTX == null) return null;
        Environment env = CTX.getEnvironment();
        return env.getProperty(key);
    }
}
