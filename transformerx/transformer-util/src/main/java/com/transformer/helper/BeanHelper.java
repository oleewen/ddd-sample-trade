package com.transformer.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.transformer.exception.helper.ExceptionHelper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.SerializationUtils;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 扩展Apache的BeanUtil
 * User: only
 * Date: 2014/8/22
 * Time: 21:37
 *
 * @author yurui
 */
public abstract class BeanHelper {
    private BeanHelper() {
    }

    /**
     * 拷贝（浅克隆）相同名称属性的值, 不同类型的属性会尝试转换
     *
     * @param desc 目标对象
     * @param orig 原始对象
     * @param <T>  对象类型
     * @return 目标对象
     */
    public static <T> T copyProperties(T desc, Object orig) {
        try {
            org.springframework.beans.BeanUtils.copyProperties(orig, desc);
        } catch (Exception e) {
            throw ExceptionHelper.createThrowableRuntimeException(e);
        }
        return desc;
    }

    /**
     * 克隆（深克隆）对象
     *
     * @param source 原始对象
     * @param <T>    对象类型
     * @return 目标对象
     */
    public static <T extends Serializable> T deepClone(T source) {
        // Kryo kryo = KryoUtils.get();
        // 使用Kryo进行深克隆
        // T cloned = kryo.copy(source);

        return SerializationUtils.clone(source);
    }

    public static Map<String, Object> toMap(Object object) {
        if (object == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }

        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(object.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            Map<String, Object> result = Maps.newHashMap();

            for (PropertyDescriptor property : propertyDescriptors) {
                String key = property.getName();

                // 得到property对应的getter方法
                Method getter = property.getReadMethod();

                // 过滤class属性，且属性无写方法
                if (!key.equals("class") && property.getWriteMethod() != null && getter != null) {

                    Object value = getter.invoke(object);

                    if (value != null) {
                        result.put(key, value);
                    }
                }
            }
            return result;
        } catch (Exception e) {
            throw ExceptionHelper.createThrowableRuntimeException(e);
        }
    }

    /**
     * 取实现类的泛型数组
     *
     * @return 泛型数组
     */
    public static Type[] getGenericTypes(Class<?> clazz) {
        if (clazz.isAssignableFrom(Object.class)) {
            throw new UnsupportedOperationException("this is a sub instance of Object");
        }

        Type type;
        do {
            // 取泛型参数
            type = clazz.getGenericSuperclass();
            clazz = clazz.getSuperclass();
        }
        // 从当前类向上，直到找到泛型类
        while (!(type instanceof ParameterizedType));
        // 取泛型集合
        return ((ParameterizedType) type).getActualTypeArguments();
    }

    /**
     * 新建结果对象，该方法由子类实现
     *
     * @return 结果对象
     */
    public static <T> T instance(Type type) {
        // 用RESULT泛型
        Class<T> clazz;
        if (type instanceof Class) {
            clazz = (Class<T>) type;
        }
        // 泛型
        else {
            clazz = (Class<T>) ((ParameterizedType) type).getRawType();
        }
        // 创建对象
        return instance(clazz);
    }

    /**
     * 创建clazz的实例
     *
     * @param clazz 类型信息
     * @return clazz实例
     */
    public static <T> T instance(Class<T> clazz) {
        T result = null;
        try {
            result = clazz.newInstance();
        } catch (Exception e) {
            throw ExceptionHelper.createNestedException(e);
        }
        return result;
    }

    public static <T> boolean isAnnotationPropertyNotEquals(T before, T after, Class<? extends Annotation> annotationClass) {
        // before == null
        if (Objects.isNull(before)) {
            return !Objects.isNull(after);
        }
        // before !=null && after == null
        else if (Objects.isNull(after)) {
            return true;
        }
        // before !=null && after != null
        List<Field> fields = BeanHelper.getAllDeclaredFields(before.getClass());

        for (Field field : fields) {
            field.setAccessible(true);

            // 被注解标注过
            Annotation annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {

                Object beforeValue = BeanHelper.getFieldValue(before, field.getName());
                Object afterValue = BeanHelper.getFieldValue(after, field.getName());

                if (!Objects.equals(beforeValue, afterValue)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static <O, V> V getFieldValue(O object, String fieldName) {
        try {
            PropertyDescriptor descriptor = new PropertyDescriptor(fieldName, object.getClass());

            return (V) descriptor.getReadMethod().invoke(object);
        } catch (Exception e) {
            throw ExceptionHelper.createNestedException(e);
        }
    }

    public static List<Field> getAllDeclaredFields(Class<?> clazz) {
        List<Field> result = Lists.newArrayList();

        while (clazz != null && !clazz.isAssignableFrom(Object.class)){
            Field[] fields = clazz.getDeclaredFields();
            if (ArrayUtils.isNotEmpty(fields)) {
                result.addAll(Arrays.asList(fields));
            }
            clazz = clazz.getSuperclass();
        }

        return result;
    }
}
