package com.nibm.config;

import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();

    static {
        try (InputStream in = AppConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {
            props.load(in);
        } catch (Exception e) {
            throw new RuntimeException("Cannot load config.properties", e);
        }
    }

    public static String get(String key) {
        return props.getProperty(key);
    }

    public static int getInt(String key) {
        return Integer.parseInt(props.getProperty(key));
    }
}