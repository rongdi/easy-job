package com.rdpaas.task.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * spring上下文工具
 * @author rongdi
 * @date 2019-03-12 19:05
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext contex)
            throws BeansException {
        SpringContextUtil.context = contex;
    }

    public static ApplicationContext getApplicationContext() {
        return context;
    }

    public static Object getBean(String beanName) {
        return context.getBean(beanName);
    }

    public static Object getBean(Class clazz) {
        return context.getBean(clazz);
    }

    public static <T> T getByTypeAndName(Class<T> clazz,String name) {
        Map<String,T> clazzMap = context.getBeansOfType(clazz);
        return clazzMap.get(name);
    }
}
