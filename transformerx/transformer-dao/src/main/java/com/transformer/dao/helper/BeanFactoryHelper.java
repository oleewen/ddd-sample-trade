package com.transformer.dao.helper;


import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.stereotype.Component;


@Component
public  class BeanFactoryHelper implements BeanFactoryAware {
    private static ConfigurableBeanFactory beanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        BeanFactoryHelper.beanFactory = (ConfigurableBeanFactory) beanFactory;
    }

    public static ConfigurableBeanFactory getBeanFactory() {
        return beanFactory;
    }

    public static void setBeanFactory(ConfigurableBeanFactory beanFactory) {
        BeanFactoryHelper.beanFactory = beanFactory;
    }

    public static <T> T getBean(Class<T> clazz, Object ... args) {
        return beanFactory.getBean(clazz, args);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return beanFactory.getBean(name, clazz);
    }

    public static Object getBean(String name, Object ... args) {
        return beanFactory.getBean(name, args);
    }



    public static void registerBean(Class<?> clazz) {
        BeanDefinition beanDefinition = BeanDefinitionBuilder.genericBeanDefinition(clazz).getRawBeanDefinition();
        registerBean(clazz.getCanonicalName(), beanDefinition);
    }

    public static void registerBean(String beanName, BeanDefinition beanDefinition) {
        ((BeanDefinitionRegistry) beanFactory).registerBeanDefinition(beanName, beanDefinition);
    }

    public static <T> T registerAndGetBean(Class<T> clazz, Object ... args) {
        T bean = getBean(clazz, args);
        if (null == bean) {
            registerBean(clazz);
            bean = getBean(clazz, args);
        }
        return bean;
    }


}
