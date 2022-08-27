package com.lin.demo.utils;

public class Config {

    private static long httpIdelTimeout = 5000;
    private static long httpMonitorInterval = 5000;
    private static int httpMaxPoolSize = 30;
    private static int httpConnectTimeout = 30;
    private static int httpSocketTimeout = 60;

    public static long getHttpIdelTimeout() {
        return httpIdelTimeout;
    }

    public static long getHttpMonitorInterval() {
        return httpMonitorInterval;
    }

    public static int getHttpMaxPoolSize() {
        return httpMaxPoolSize;
    }

    public static int getHttpConnectTimeout() {
        return httpConnectTimeout;
    }

    public static int getHttpSocketTimeout() {
        return httpSocketTimeout;
    }
}
