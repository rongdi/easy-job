package com.rdpaas.task.annotation;

import com.rdpaas.task.common.Invocation;
import com.rdpaas.task.scheduler.TaskExecutor;
import com.rdpaas.task.utils.Delimiters;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * spring容器启动完后，加载自定义注解
 * @author rongdi
 * @date 2019-03-15 21:07
 */
@Component
public class ContextRefreshedListener implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private TaskExecutor taskExecutor;

    /**
     * 用来保存方法名/任务名和任务插入后数据库的ID的映射,用来处理子任务新增用
     */
    private Map<String,Long> taskIdMap = new HashMap<>();

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
             * 用来存放被调度注解修饰的方法名和Method的映射
             */
            Map<String,Method> methodMap = new HashMap<>();
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
                        methodMap.put(clazz.getName() + Delimiters.DOT + m.getName(),m);
                    }
                }
            }
            /**
             * 处理Sheduled注解
             */
            handleSheduledAnn(methodMap);
            /**
             * 由于taskIdMap只是启动spring完成后使用一次，这里可以直接清空
             */
            taskIdMap.clear();
        }
    }

    /**
     * 循环处理方法map中的所有Method
     * @param methodMap
     */
    private void handleSheduledAnn(Map<String,Method> methodMap) {
        if(methodMap == null || methodMap.isEmpty()) {
            return;
        }
        Set<Map.Entry<String,Method>> entrys = methodMap.entrySet();
        /**
         * 遍历bean和里面的method找到被Scheduled注解修饰的方法,然后将任务放入任务调度里
         */
        for(Map.Entry<String,Method> entry:entrys){
            Method m = entry.getValue();
            try {
                handleSheduledAnn(methodMap,m);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
        }
    }

    /**
     * 递归添加父子任务
     * @param methodMap
     * @param m
     * @throws Exception
     */
    private void handleSheduledAnn(Map<String,Method> methodMap,Method m) throws Exception {
        Class<?> clazz = m.getDeclaringClass();
        String name = m.getName();
        Scheduled sAnn = m.getAnnotation(Scheduled.class);
        String cron = sAnn.cron();
        String parent = sAnn.parent();
        /**
         * 如果parent为空，说明该方法代表的任务是根任务，则添加到任务调度器中，并且保存在全局map中
         * 如果parent不为空，则表示是子任务，子任务需要知道父任务的id
         * 先根据parent里面代表的方法全名或者方法名（父任务方法和子任务方法在同一个类直接可以用方法名，
         * 不然要带上类的全名）从taskIdMap获取父任务ID
         * 如果找不到父任务ID，先根据父方法全名在methodMap找到父任务的method对象，调用本方法递归下去
         * 如果找到父任务ID，则添加子任务
         */
        if(StringUtils.isEmpty(parent)) {
            if(!taskIdMap.containsKey(clazz.getName() + Delimiters.DOT + name)) {
                Long taskId = taskExecutor.addTask(name, cron, new Invocation(clazz, name, new Class[]{}, new Object[]{}));
                taskIdMap.put(clazz.getName() + Delimiters.DOT + name, taskId);
            }
        } else {
            String parentMethodName = parent.lastIndexOf(Delimiters.DOT) == -1 ? clazz.getName() + Delimiters.DOT + parent : parent;
            Long parentTaskId = taskIdMap.get(parentMethodName);
            if(parentTaskId == null) {
                Method parentMethod = methodMap.get(parentMethodName);
                handleSheduledAnn(methodMap,parentMethod);
                /**
                 * 递归回来一定要更新一下这个父任务ID
                 */
                parentTaskId = taskIdMap.get(parentMethodName);
            }
            if(parentTaskId != null && !taskIdMap.containsKey(clazz.getName() + Delimiters.DOT + name)) {
                Long taskId = taskExecutor.addChildTask(parentTaskId, name, cron, new Invocation(clazz, name, new Class[]{}, new Object[]{}));
                taskIdMap.put(clazz.getName() + Delimiters.DOT + name, taskId);
            }

        }


    }
}
