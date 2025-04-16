package com.transformer.context;

/**
 * 环境枚举
 * User: only
 * Date: 2014-1-20
 * Time: 下午5:07
 */
public enum Env {
    /** 本地开发环境 */
    DEV("dev"),
    /** 日常测试环境 */
    FAT("fat"),
    /** 标准测试环境 */
    TEST("test"),
    /** 预发环境 */
    PRE("uat"),
    /** sandbox沙箱环境 */
    SANDBOX("sandbox"),
    /** 生产环境 */
    PRODUCT("pro");
    /** 环境系统参数名 */
    public static final String ENV_NAME = "context.env";
    /** 环境名称 */
    private final String envName;

    /**
     * 构造函数
     *
     * @param env 环境名称
     */
    private Env(String env) {
        this.envName = env;
    }

    /**
     * 工厂方法
     *
     * @param env 环境名称
     * @return 环境枚举
     */
    public static Env instance(String env) {
        Env[] values = Env.values();
        for (Env each : values) {
            if (each.getEnvName().equals(env)) {
                return each;
            }
        }
        return DEV;
    }

    /**
     * 是否现网环境：idc，beta，pre
     *
     * @return 是否现网环境
     */
    public boolean isOnline() {
        return isProd() || isPre() || isSandbox();
    }

    /**
     * 是否线下环境：daily，dev
     *
     * @return 是否线下环境
     */
    public boolean isOffline() {
        return isDaily() || isTest() || isDev();
    }

    /**
     * 判断是否dev环境
     *
     * @return 是否dev环境
     */
    public boolean isDev() {
        return this == DEV;
    }

    /**
     * 判断是否daily环境
     *
     * @return 是否daily环境
     */
    public boolean isDaily() {
        return this == FAT;
    }

    /**
     * 判断是否测试环境
     *
     * @return 是否测试环境
     */
    public boolean isTest() {
        return this == TEST;
    }

    /**
     * 判断是否pre环境
     *
     * @return 是否pre环境
     */
    public boolean isPre() {
        return this == PRE;
    }

    /**
     * 判断是否sandbox环境
     *
     * @return 是否sandbox环境
     */
    public boolean isSandbox() {
        return this == SANDBOX;
    }

    /**
     * 判断是否生产环境
     *
     * @return 是否生产环境
     */
    public boolean isProd() {
        return this == PRODUCT;
    }

    /**
     * 取环境名称
     *
     * @return 环境名称
     */
    public String getEnvName() {
        return envName;
    }

}
