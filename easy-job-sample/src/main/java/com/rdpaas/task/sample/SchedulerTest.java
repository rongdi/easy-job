package com.rdpaas.task.sample;

import com.rdpaas.task.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 测试调度功能
 * @author rongdi
 * @date 2019-03-17 16:54
 */
@Component
public class SchedulerTest {

	@Scheduled(cron = "0/10 * * * * ?")
    public void test1() throws InterruptedException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Thread.sleep(2000);
        System.out.println("当前时间1:"+sdf.format(new Date()));
    }
	
	@Scheduled(cron = "0/20 * * * * ?",parent = "test1")
    public void test2() throws InterruptedException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Thread.sleep(2000);
        System.out.println("当前时间2:"+sdf.format(new Date()));
    }

    @Scheduled(cron = "0/10 * * * * ?",parent = "test2")
    public void test3() throws InterruptedException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Thread.sleep(2000);
        System.out.println("当前时间3:"+sdf.format(new Date()));
    }

    @Scheduled(cron = "0/10 * * * * ?",parent = "test3")
    public void test4() throws InterruptedException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Thread.sleep(2000);
        System.out.println("当前时间4:"+sdf.format(new Date()));
    }

}
