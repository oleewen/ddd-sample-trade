/**
 * taobao.com Inc. Copyright (c) 2009-2012 All Rights Reserved.
 */
package com.transformer.context;

import org.apache.commons.lang3.StringUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 上下文环境类
 *
 * @author only 2012-5-15
 */
public class Context {

    /** 机器ip */
    private static String ip;
    /** 机器名 */
    private static String machine;
    /** 环境 */
    private static String env;
    /** 上下文附件集合 */
    private static Map<String, Object> attachments = new ConcurrentHashMap<>();
    /** 上下文ThreadLocal集合 */
    private static Map<String, ThreadLocal<?>> threadLocals = new ConcurrentHashMap<>();

    static {
        try {
            Context.ip = InetAddress.getLocalHost().getHostAddress();
            Context.machine = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // do nothing
        }
        Context.env = System.getProperty(Env.ENV_NAME);
    }

    /**
     * 设置键值对到上下文
     *
     * @param key   键
     * @param value 值
     * @return 返回key对应的旧对象
     */
    public static <T> T put(String key, T value) {
        Object old = attachments.put(key, value);
        return value != null ? (T) old : null;
    }

    /**
     * 取上下文中key对应的对象
     *
     * @param key 键
     * @return key对应的值对象
     */
    public static <T> T get(String key) {
        Object value = attachments.get(key);
        return value != null ? (T) value : null;
    }

    /**
     * 设置键值对到ThreadLocal
     *
     * @param key   键
     * @param value 值
     * @return 返回key对应的旧对象
     */
    public static <T> T putThreadLocal(String key, T value) {
        ThreadLocal<T> threadLocal = (ThreadLocal<T>) threadLocals.computeIfAbsent(key, (Function<? super String, ? extends ThreadLocal<?>>) new ThreadLocal<>());

        T old = threadLocal.get();
        threadLocal.set(value);
        return old;
    }

    /**
     * 取ThreadLocal中key对应的对象
     *
     * @param key 键
     * @return key对应的值对象
     */
    public static <T> T getThreadLocal(String key) {
        ThreadLocal<T> threadLocal = (ThreadLocal<T>) threadLocals.get(key);
        if (threadLocal == null) {
            return null;
        }
        return threadLocal.get();
    }

    /**
     * get ip
     *
     * @return the ip
     */
    public static String getIp() {
        return ip;
    }

    /**
     * get machine
     *
     * @return the machine
     */
    public static String getMachine() {
        return machine;
    }

    /**
     * get env
     *
     * @return the env
     */
    public static String getEnv() {
        return env;
    }

    /**
     * @param env environment
     */
    public static void setEnv(String env) {
        // 变量context.env不存在，记录环境变量
        if (StringUtils.isBlank(Context.env)) {
            Context.env = env;
        }
        // 变量context.env存在时，以系统参数为准
    }

    /**
     * get nocache
     *
     * @return the nocache
     */
    public static boolean debug() {
        Boolean debug = getThreadLocal(ContextLocal.DEBUG.name());
        return debug != null && Boolean.TRUE.equals(debug);
    }

    /**
     * @param debug debug flag
     * @return set success
     */
    public static boolean setDebug(boolean debug) {
        Boolean old = putThreadLocal(ContextLocal.DEBUG.name(), debug);
        return old != null && Boolean.TRUE.equals(old);
    }

    /**
     * get nocache
     *
     * @return the nocache
     */
    public static boolean nocache() {
        Boolean nocache = getThreadLocal(ContextLocal.NOCACHE.name());
        return nocache != null && Boolean.TRUE.equals(nocache);
    }

    /**
     * @param nocache nocache flag
     * @return set success
     */
    public static boolean setNocache(boolean nocache) {
        Boolean old = putThreadLocal(ContextLocal.NOCACHE.name(), nocache);
        return old != null && Boolean.TRUE.equals(old);
    }

    /** 上下文本地变量 */
    public enum ContextLocal {
        NOCACHE,
        DEBUG
    }
}
