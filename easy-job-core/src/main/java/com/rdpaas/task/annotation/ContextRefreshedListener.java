package com.rdpaas.task.annotation;

import com.rdpaas.task.common.Invocation;
import com.rdpaas.task.scheduler.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * spring容器启动完后，加载自定义注解
 */
@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private TaskExecutor taskExecutor;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        /**
         * 判断根容器为Spring容器，防止出现调用两次的情况（mvc加载也会触发一次）
          */
        if(event.getApplicationContext().getParent()==null){
            /**
             * 判断调度开关是否打开
             * 如果打开了：加载调度注解并将调度添加到调度管理中
             */
            ApplicationContext context = event.getApplicationContext();
            Map<String,Object> beans = context.getBeansWithAnnotation(org.springframework.scheduling.annotation.EnableScheduling.class);
            if(beans == null) {
                return;
            }
            /**
             * 查找所有直接或者间接被Component注解修饰的类，因为不管Service，Controller等都包含了Component，也就是
             * 只要是被纳入了spring容器管理的类必然直接或者间接的被Component修饰
             */
            Map<String,Object> allBeans = context.getBeansWithAnnotation(org.springframework.stereotype.Component.class);
            Set<Map.Entry<String,Object>> entrys = allBeans.entrySet();
            /**
             * 遍历bean和里面的method找到被Scheduled注解修饰的方法,然后将任务放入任务调度里
             */
            for(Map.Entry entry:entrys){
                Object obj = entry.getValue();
                Class clazz = obj.getClass();
                Method[] methods = clazz.getMethods();
                for(Method m:methods) {
                    if(m.isAnnotationPresent(Scheduled.class)) {
                        Scheduled sAnn = m.getAnnotation(Scheduled.class);
                        String cron = sAnn.cron();
                        String name = m.getName();
                        try {
                            taskExecutor.addTask(name,cron,new Invocation(clazz,name,new Class[]{},new Object[]{}));
                        } catch (Exception e) {
                            continue;
                        }
                    }
                }
            }
        }
    }
}
