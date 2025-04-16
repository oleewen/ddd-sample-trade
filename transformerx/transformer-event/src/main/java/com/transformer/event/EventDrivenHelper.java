package com.transformer.event;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 文件描述：事件驱动辅助类
 *
 * @author only
 *         Date 2015/8/4.
 */
public abstract class EventDrivenHelper {
    private static final String DEFAULT_GROUP = "default";
    /** 事件巴士集合 */
    private static ConcurrentMap<String, EventBus> eventBusStation = Maps.newConcurrentMap();
    private static ExecutorService executor = Executors.newCachedThreadPool();

    private EventDrivenHelper(){}
    /**
     * 发布事件：异步执行
     *
     * @param event 事件对象
     */
    public static void publishEvent(Event event) {
        // 默认分组
        publishEvent(DEFAULT_GROUP, event);
    }

    /**
     * 发布事件：异步执行
     *
     * @param group 事件分组
     * @param event 事件对象
     */
    public static void publishEvent(String group, Event event) {
        Preconditions.checkNotNull(event, "event is required");

        // 初始化事件巴士
        String key = initEventBus(group, true);

        // 发布事件
        eventBusStation.get(key).post(event);
    }

    /**
     * 触发事件：同步执行
     *
     * @param event 事件对象
     */
    public static void fireEvent(Event event) {
        // 默认分组
        fireEvent(DEFAULT_GROUP, event);
    }

    /**
     * 触发事件：同步执行
     *
     * @param group 事件分组
     * @param event 事件对象
     */
    public static void fireEvent(String group, Event event) {
        Preconditions.checkNotNull(event, "event is required");

        // 初始化事件巴士
        String key = initEventBus(group, false);

        // 发布事件
        eventBusStation.get(key).post(event);
    }

    /**
     * 注册监听器，采用默认分组
     *
     * @param listener 监听器
     */
    public static void registerListener(Object listener) {
        // 默认分组
        registerListener(DEFAULT_GROUP, listener);
    }

    /**
     * 注册监听器到group分组：异步执行
     *
     * @param group    事件分组
     * @param listener 监听器
     */
    public static void registerListener(String group, Object listener) {
        Preconditions.checkNotNull(listener, "listener is required");

        // 初始化异步事件巴士
        String asyncKey = initEventBus(group, true);

        eventBusStation.get(asyncKey).register(listener);

        // 初始化同步事件巴士
        String syncKey = initEventBus(group, false);

        eventBusStation.get(syncKey).register(listener);
    }

    private static String initEventBus(String group, boolean async) {
        // 计算group key
        String key = getGroupKey(group, async);

        // 未初始化时，初始化一次
        if (!eventBusStation.containsKey(key)) {
            eventBusStation.putIfAbsent(key, async ? new AsyncEventBus(key, executor) : new EventBus(key));
        }

        return key;
    }

    private static String getGroupKey(String group, boolean async) {
        if (StringUtils.isEmpty(group)) {
            group = DEFAULT_GROUP;
        }

        // 异步
        if (async) {
            group += "_async";
        }

        return group;
    }
}