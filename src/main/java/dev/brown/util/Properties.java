package dev.brown.util;

import java.util.Map;

public class Properties {

    private Properties() {

    }

    public static String getEnvValue(final String name) {
        return System.getenv(name);
    }

    public static Map<String, String> getEnvValue() {
        return System.getenv();
    }
}
