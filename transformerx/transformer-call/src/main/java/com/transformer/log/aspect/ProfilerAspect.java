package com.transformer.log.aspect;

import com.transformer.consts.StringConst;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 性能分析AOP切面：拦截有Profiler注解的方法，进行性能分析
 * <pre>
 @Configuration
 @EnableAspectJAutoProxy
 @ComponentScan
 public class SpringConfig {
      @Bean
      public ProfilerAspect profilerAspect() {
          return new ProfilerAspect();
      }
 }
 * </pre>
 * @author only
 * @date 2014-07-14
 */
@Aspect
public class ProfilerAspect {
    /** 日志对象 */
    private static Logger logger = LoggerFactory.getLogger(ProfilerAspect.class);
    /** 随机采样频率 */
    private static Random random = new Random();
    /** 是否有方法超时 */
    private ThreadLocal<Boolean> timeout = new ThreadLocal<>();
    /** 是否首次进入 */
    private ThreadLocal<Boolean> topFlag = new ThreadLocal<>();

    @Pointcut("@annotation(com.zto.tms.transformer.log.aspect.Profiler)")
    public void profilerPoint() {
    }

    /**
     * profiler方法拦截
     *
     * @param joinPoint 连接点
     */
    @Around("profilerPoint()")
    public Object profile(ProceedingJoinPoint joinPoint) throws Throwable {
        // 方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(), signature.getMethod().getParameterTypes());

        com.transformer.log.annotation.Profiler profiler = method.getAnnotation(com.transformer.log.annotation.Profiler.class);

        String profilerName = StringUtils.defaultIfBlank(profiler.value(), signature.toString());

        // 检测是否是Profiler注解的顶层调用，只有顶层调用才start并打日志
        boolean top = false;
        if (topFlag.get() == null) {
            top = true;
            topFlag.set(Boolean.TRUE);
            // 开启Profiler
            Profiler.start(profilerName);
        }
        // 进入当前方法
        Profiler.enter(profilerName);

        long start = System.currentTimeMillis();

        Object result;
        try {
            // 执行方法
            result = joinPoint.proceed();
        } finally {
            long end = System.currentTimeMillis();
            // 有步骤超时
            if (end - start > profiler.elapsed()) {
                timeout.set(Boolean.TRUE);
            }
            // 退出当前方法
            Profiler.release();

            // Profiler注解的顶层调用release start
            if (top) {
                // 退出Profiler
                Profiler.release();

                // 打印性能日志
                logger(profiler);

                // 重置删除entry，防止线程复用的获取到entry
                Profiler.reset();
                timeout.remove();
                topFlag.remove();
            }
        }
        return result;
    }

    private void logger(com.transformer.log.annotation.Profiler profiler) {
        // 整体超时或者中间步骤超时，打印性能日志
        if (Profiler.getDuration() > profiler.elapsed() || Boolean.TRUE.equals(timeout.get())) {
            logger.error(String.format("timeout@elapsed:%d,duration:%d,profile:%s", profiler.elapsed(), Profiler.getDuration(), Profiler.dump()));
        }
        // 或者符合采样频率条件
        else if (random.nextInt(profiler.basic()) <= profiler.sample()) {
            logger.warn(String.format("sample@elapsed:%d,duration:%d,profile:%s", profiler.elapsed(), Profiler.getDuration(), Profiler.dump()));
        }
    }

    /**
     * 用来测试并统计线程执行时间的工具。
     *
     * @author Michael Zhou
     * @version $Id: Profiler.java 1291 2005-03-04 03:23:30Z baobao $
     */
    public static final class Profiler {
        private static final ThreadLocal<Profiler.Entry> entryStack = new ThreadLocal<>();

        private Profiler(){}

        /**
         * 开始计时。
         */
        public static void start() {
            start((String) null);
        }

        /**
         * 开始计时。
         *
         * @param message 第一个entry的信息
         */
        public static void start(String message) {
            entryStack.set(new Profiler.Entry(message, null, null));
        }

        /**
         * 清除计时器。
         *
         * <p>
         * 清除以后必须再次调用<code>start</code>方可重新计时。
         * </p>
         */
        public static void reset() {
            entryStack.remove();
        }

        /**
         * 开始一个新的entry，并计时。
         *
         * @param message 新entry的信息
         */
        public static void enter(String message) {
            Profiler.Entry currentEntry = getCurrentEntry();

            if (currentEntry != null) {
                currentEntry.enterSubEntry(message);
            }
        }

        /**
         * 结束最近的一个entry，记录结束时间。
         */
        public static void release() {
            Profiler.Entry currentEntry = getCurrentEntry();

            if (currentEntry != null) {
                currentEntry.release();
            }
        }

        /**
         * 取得耗费的总时间。
         *
         * @return 耗费的总时间，如果未开始计时，则返回<code>-1</code>
         */
        public static long getDuration() {
            Profiler.Entry entry = entryStack.get();

            if (entry != null) {
                return entry.getDuration();
            } else {
                return -1;
            }
        }

        /**
         * 列出所有的entry。
         *
         * @return 列出所有entry，并统计各自所占用的时间
         */
        public static String dump() {
            return dump("", "");
        }

        /**
         * 列出所有的entry。
         *
         * @param prefix 前缀
         *
         * @return 列出所有entry，并统计各自所占用的时间
         */
        public static String dump(String prefix) {
            return dump(prefix, prefix);
        }

        /**
         * 列出所有的entry。
         *
         * @param prefix1 首行前缀
         * @param prefix2 后续行前缀
         *
         * @return 列出所有entry，并统计各自所占用的时间
         */
        public static String dump(String prefix1, String prefix2) {
            Profiler.Entry entry = entryStack.get();

            if (entry != null) {
                return entry.toString(prefix1, prefix2);
            } else {
                return StringConst.EMPTY;
            }
        }

        /**
         * 取得第一个entry。
         *
         * @return 第一个entry，如果不存在，则返回<code>null</code>
         */
        public static Profiler.Entry getEntry() {
            return entryStack.get();
        }

        /**
         * 取得最近的一个entry。
         *
         * @return 最近的一个entry，如果不存在，则返回<code>null</code>
         */
        private static Profiler.Entry getCurrentEntry() {
            Profiler.Entry subEntry = entryStack.get();
            Profiler.Entry entry = null;

            if (subEntry != null) {
                do {
                    entry    = subEntry;
                    subEntry = entry.getUnreleasedEntry();
                } while (subEntry != null);
            }

            return entry;
        }

        /**
         * 代表一个计时单元。
         */
        public static final class Entry {
            private final List<Entry> subEntries  = new ArrayList<>(4);
            private final Object message;
            private final Profiler.Entry parentEntry;
            private final Profiler.Entry firstEntry;
            private final long   baseTime;
            private final long   startTime;
            private long         endTime;

            /**
             * 创建一个新的entry。
             *
             * @param message entry的信息，可以是<code>null</code>
             * @param parentEntry 父entry，可以是<code>null</code>
             * @param firstEntry 第一个entry，可以是<code>null</code>
             */
            private Entry(Object message, Profiler.Entry parentEntry, Profiler.Entry firstEntry) {
                this.message     = message;
                this.startTime   = System.currentTimeMillis();
                this.parentEntry = parentEntry;
                this.firstEntry  = ObjectUtils.defaultIfNull(firstEntry, this);
                this.baseTime    = (firstEntry == null) ? 0
                        : firstEntry.startTime;
            }

            /**
             * 取得entry的信息。
             */
            public String getMessage() {
                String messageString = null;

                if (message instanceof String) {
                    messageString = (String) message;
                }

                return StringUtils.defaultIfEmpty(messageString, null);
            }

            /**
             * 取得entry相对于第一个entry的起始时间。
             *
             * @return 相对起始时间
             */
            public long getStartTime() {
                return (baseTime > 0) ? (startTime - baseTime)
                        : 0;
            }

            /**
             * 取得entry相对于第一个entry的结束时间。
             *
             * @return 相对结束时间，如果entry还未结束，则返回<code>-1</code>
             */
            public long getEndTime() {
                if (endTime < baseTime) {
                    return -1;
                } else {
                    return endTime - baseTime;
                }
            }

            /**
             * 取得entry持续的时间。
             *
             * @return entry持续的时间，如果entry还未结束，则返回<code>-1</code>
             */
            public long getDuration() {
                if (endTime < startTime) {
                    return -1;
                } else {
                    return endTime - startTime;
                }
            }

            /**
             * 取得entry自身所用的时间，即总时间减去所有子entry所用的时间。
             *
             * @return entry自身所用的时间，如果entry还未结束，则返回<code>-1</code>
             */
            public long getDurationOfSelf() {
                long duration = getDuration();

                if (duration < 0) {
                    return -1;
                } else if (subEntries.isEmpty()) {
                    return duration;
                } else {
                    for (int i = 0; i < subEntries.size(); i++) {
                        Profiler.Entry subEntry = subEntries.get(i);

                        duration -= subEntry.getDuration();
                    }

                    if (duration < 0) {
                        return -1;
                    } else {
                        return duration;
                    }
                }
            }

            /**
             * 取得当前entry在父entry中所占的时间百分比。
             *
             * @return 百分比
             */
            public double getPercentage() {
                double parentDuration = 0;
                double duration = getDuration();

                if ((parentEntry != null) && parentEntry.isReleased()) {
                    parentDuration = parentEntry.getDuration();
                }

                if ((duration > 0) && (parentDuration > 0)) {
                    return duration / parentDuration;
                } else {
                    return 0;
                }
            }

            /**
             * 取得当前entry在第一个entry中所占的时间百分比。
             *
             * @return 百分比
             */
            public double getPercentageOfAll() {
                double firstDuration = 0;
                double duration = getDuration();

                if ((firstEntry != null) && firstEntry.isReleased()) {
                    firstDuration = firstEntry.getDuration();
                }

                if ((duration > 0) && (firstDuration > 0)) {
                    return duration / firstDuration;
                } else {
                    return 0;
                }
            }

            /**
             * 取得所有子entries。
             *
             * @return 所有子entries的列表（不可更改）
             */
            public List<Entry> getSubEntries() {
                return Collections.unmodifiableList(subEntries);
            }

            /**
             * 结束当前entry，并记录结束时间。
             */
            private void release() {
                endTime = System.currentTimeMillis();
            }

            /**
             * 判断当前entry是否结束。
             *
             * @return 如果entry已经结束，则返回<code>true</code>
             */
            private boolean isReleased() {
                return endTime > 0;
            }

            /**
             * 创建一个新的子entry。
             *
             * @param message 子entry的信息
             */
            private void enterSubEntry(Object message) {
                Profiler.Entry subEntry = new Profiler.Entry(message, this, firstEntry);

                subEntries.add(subEntry);
            }

            /**
             * 取得未结束的子entry。
             *
             * @return 未结束的子entry，如果没有子entry，或所有entry均已结束，则返回<code>null</code>
             */
            private Profiler.Entry getUnreleasedEntry() {
                Profiler.Entry subEntry = null;

                if (!subEntries.isEmpty()) {
                    subEntry = subEntries.get(subEntries.size() - 1);

                    if (subEntry.isReleased()) {
                        subEntry = null;
                    }
                }

                return subEntry;
            }

            /**
             * 将entry转换成字符串的表示。
             *
             * @return 字符串表示的entry
             */
            @Override
            public String toString() {
                return toString("", "");
            }

            /**
             * 将entry转换成字符串的表示。
             *
             * @param prefix1 首行前缀
             * @param prefix2 后续行前缀
             *
             * @return 字符串表示的entry
             */
            private String toString(String prefix1, String prefix2) {
                StringBuilder builder = new StringBuilder();

                toString(builder, prefix1, prefix2);

                return builder.toString();
            }

            /**
             * 将entry转换成字符串的表示。
             *
             * @param builder 字符串buffer
             * @param prefix1 首行前缀
             * @param prefix2 后续行前缀
             */
            private void toString(StringBuilder builder, String prefix1, String prefix2) {
                builder.append(prefix1);

                long     duration       = getDuration();
                long     durationOfSelf = getDurationOfSelf();
                double   percent        = getPercentage();
                double   percentOfAll   = getPercentageOfAll();

                Object[] params = new Object[] {
                        // {0} - entry信息
                        getMessage(),
                        // {1} - 起始时间
                        getStartTime(),
                        // {2} - 持续总时间
                        duration,
                        // {3} - 自身消耗的时间
                        durationOfSelf,
                        // {4} - 在父entry中所占的时间比例
                        percent,
                        // {5} - 在总时间中所旧的时间比例
                        percentOfAll
                };

                StringBuilder pattern = new StringBuilder("{1,number} ");

                if (isReleased()) {
                    pattern.append("[{2,number}ms");

                    if ((durationOfSelf > 0) && (durationOfSelf != duration)) {
                        pattern.append(" ({3,number}ms)");
                    }

                    if (percent > 0) {
                        pattern.append(", {4,number,##%}");
                    }

                    if (percentOfAll > 0) {
                        pattern.append(", {5,number,##%}");
                    }

                    pattern.append("]");
                } else {
                    pattern.append("[UNRELEASED]");
                }

                if (getMessage() != null) {
                    pattern.append(" - {0}");
                }

                builder.append(MessageFormat.format(pattern.toString(), params));

                for (int i = 0; i < subEntries.size(); i++) {
                    Profiler.Entry subEntry = subEntries.get(i);

                    builder.append('\n');

                    if (i == (subEntries.size() - 1)) {
                        // 最后一项
                        subEntry.toString(builder, prefix2 + "`---", prefix2 + "    ");
                    } else if (i == 0) {
                        // 第一项
                        subEntry.toString(builder, prefix2 + "+---", prefix2 + "|   ");
                    } else {
                        // 中间项
                        subEntry.toString(builder, prefix2 + "+---", prefix2 + "|   ");
                    }
                }
            }
        }
    }
}
