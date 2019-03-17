package com.rdpaas.task;

import com.rdpaas.Application;
import com.rdpaas.task.common.Invocation;
import com.rdpaas.task.scheduler.TaskExecutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class TaskTest {

    @Autowired
    private TaskExecutor executor;

    @Test
    public void testTask() throws Exception {
        executor.addTask("test1","0/10 * 9-19 * * ?",new Invocation(com.rdpaas.task.Test.class,"test1",new Class[]{},new Object[]{}));
        Thread.sleep(1000);
        executor.addTask("test2","0/20 * 9-19 * * ?",new Invocation(com.rdpaas.task.Test.class,"test2",new Class[]{},new Object[]{}));
        Thread.sleep(1000);
        executor.addChildTask(1L,"test3","0/10 * 9-19 * * ?",new Invocation(com.rdpaas.task.Test.class,"test3",new Class[]{},new Object[]{}));
        Thread.sleep(1000);
        executor.addChildTask(3L,"test4","0/10 * 9-19 * * ?",new Invocation(com.rdpaas.task.Test.class,"test4",new Class[]{},new Object[]{}));
        System.in.read();
    }



}
