package com.transformer.context;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * @author tao
 */
public class Environment implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    private static ConfigurableEnvironment envConfigure;

    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        setEnvironment(event.getEnvironment());
    }

    public static String getEnvProperty(String key) {
        return getEnvironment().getProperty(key);
    }

    public static String getEnvProperty(String key, String defaultValue) {
        return getEnvironment().getProperty(key, defaultValue);
    }

    /**
     * 获取当前应用的 appId
     */
    public static String getApplicationId() {
        return com.zto.titans.common.env.EnvironmentManager.getAppName();
    }

    /**
     * 获取当前环境的注册中心地址
     */
    public static String getRegistryAddress() {
        return getEnvProperty(com.zto.titans.common.env.EnvironmentManager.DUBBO_REGISTRY_ADDRESS);
    }

    /**
     * 判断当前环境是否为 dev 环境
     */
    public static boolean isDev() {
        return isEnv(Env.DEV);
    }

    /**
     * 判断当前环境是否为 fat 环境
     */
    public static boolean isFat() {
        return isEnv(Env.FAT);
    }

    /**
     * 判断当前环境是否为 uat 环境
     */
    public static boolean isUat() {
        return isEnv(Env.PRE);
    }

    /**
     * 判断当前环境是否为 pro 环境
     */
    public static boolean isProd() {
        return isEnv(Env.PRODUCT);
    }

    private static boolean isEnv(Env env) {
        String[] activeProfiles = getEnvironment().getActiveProfiles();
        if (ArrayUtils.isEmpty(activeProfiles)) {
            return false;
        }
        String activeProfile = activeProfiles[0];
        return StringUtils.containsIgnoreCase(activeProfile, env.getEnvName());
    }

    private static ConfigurableEnvironment getEnvironment() {
        return envConfigure;
    }

    private static void setEnvironment(ConfigurableEnvironment environment) {
        envConfigure = environment;
    }
}