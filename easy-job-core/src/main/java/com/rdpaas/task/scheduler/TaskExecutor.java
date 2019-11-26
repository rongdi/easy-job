package com.rdpaas.task.scheduler;

import com.rdpaas.task.common.*;
import com.rdpaas.task.config.EasyJobConfig;
import com.rdpaas.task.repository.NodeRepository;
import com.rdpaas.task.repository.TaskRepository;
import com.rdpaas.task.strategy.Strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

/**
 * 任务调度器
 * @author rongdi
 * @date 2019-03-13 21:15
 */
@Component
public class TaskExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TaskExecutor.class);

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NodeRepository nodeRepository;

    @Autowired
    private EasyJobConfig config;

    /**
     * 创建任务到期延时队列
      */
    private DelayQueue<DelayItem<Task>> taskQueue = new DelayQueue<>();

    /**
     * 可以明确知道最多只会运行2个线程，直接使用系统自带工具就可以了
     */
    private ExecutorService bossPool = Executors.newFixedThreadPool(2);

    /**
     * 声明工作线程池
     */
    private ThreadPoolExecutor workerPool;
    
    /**
     * 获取任务的策略
     */
    private Strategy strategy;


    @PostConstruct
    public void init() {
    	/**
    	 * 根据配置选择一个节点获取任务的策略
    	 */
    	strategy = Strategy.choose(config.getNodeStrategy());
    	/**
         * 自定义线程池，初始线程数量corePoolSize，线程池等待队列大小queueSize，当初始线程都有任务，并且等待队列满后
         * 线程数量会自动扩充最大线程数maxSize，当新扩充的线程空闲60s后自动回收.自定义线程池是因为Executors那几个线程工具
         * 各有各的弊端，不适合生产使用
         */
        workerPool = new ThreadPoolExecutor(config.getCorePoolSize(), config.getMaxPoolSize(), 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(config.getQueueSize()));
        /**
         * 执行待处理任务加载线程
         */
        bossPool.execute(new Loader());
        /**
         * 执行任务调度线程
         */
        bossPool.execute(new Boss());
    
    }

    class Loader implements Runnable {

		@Override
		public void run() {
			for(;;) {
				try { 
					/**
	                 * 先获取可用的节点列表
	                 */
	                List<Node> nodes = nodeRepository.getEnableNodes(config.getHeartBeatSeconds() * 2);
	            	if(nodes == null || nodes.isEmpty()) {
	            		continue;
	            	}
	                /**
	                 * 查找还有指定时间(单位秒)才开始的主任务列表
	                 */
	                List<Task> tasks = taskRepository.listNotStartedTasks(config.getFetchDuration());
	                if(tasks == null || tasks.isEmpty()) {
	                	continue;
	                }
	                for(Task task:tasks) {
	                	
	                	boolean accept = strategy.accept(nodes, task, config.getNodeId());
                        /**
                         * 不该自己拿就不要抢
                         */
                    	if(!accept) {
                        	continue;
                        }
                        /**
                         * 先设置成待执行
                         */
                    	task.setStatus(TaskStatus.PENDING);
                        task.setNodeId(config.getNodeId());
                        /**
                         * 使用乐观锁尝试更新状态，如果更新成功，其他节点就不会更新成功。如果其它节点也正在查询未完成的
                         * 任务列表和当前这段时间有节点已经更新了这个任务，version必然和查出来时候的version不一样了,这里更新
                         * 必然会返回0了
                         */
                        int n = taskRepository.updateWithVersion(task);
                        Date nextStartTime = task.getNextStartTime();
                        if(n == 0 || nextStartTime == null) {
                        	continue;
                        }
                        /**
                         * 封装成延时对象放入延时队列,这里再查一次是因为上面乐观锁已经更新了版本，会导致后面结束任务更新不成功
                         */
                        task = taskRepository.get(task.getId());
                        DelayItem<Task> delayItem = new DelayItem<Task>(nextStartTime.getTime() - new Date().getTime(), task);
                        taskQueue.offer(delayItem);
                    	
	                }
	                Thread.sleep(config.getFetchPeriod());
				} catch(Exception e) {
					logger.error("fetch task list failed,cause by:{}", e);
				}
			}
		}
    	
    }
    
    class Boss implements Runnable {
        @Override
        public void run() {
            for (;;) {
                try {
                	 /**
                     * 时间到了就可以从延时队列拿出任务对象,然后交给worker线程池去执行
                     */
                    DelayItem<Task> item = taskQueue.take();
                    if(item != null && item.getItem() != null) {
                        Task task = item.getItem();
                        /**
                         * 真正开始执行了设置成执行中
                         */
                        task.setStatus(TaskStatus.DOING);
                        /**
                         * loader线程中已经使用乐观锁控制了，这里没必要了
                         */
                        taskRepository.update(task);
                        workerPool.execute(new Worker(task));
                    }
                	 
                } catch (Exception e) {
                    logger.error("fetch task failed,cause by:{}", e);
                }
            }
        }

    }

    class Worker implements Runnable {

        private Task task;

        public Worker(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            logger.info("Begin to execute task:{}",task.getId());
            TaskDetail detail = null;
            try {
                //开始任务
                detail = taskRepository.start(task);
                if(detail == null) return;
                //执行任务
                task.getInvokor().invoke();
                //完成任务
                finish(task,detail);
                logger.info("finished execute task:{}",task.getId());
            } catch (Exception e) {
                logger.error("execute task:{} error,cause by:{}",task.getId(), e);
                try {
                    taskRepository.fail(task,detail,e.getCause().getMessage());
                } catch(Exception e1) {
                    logger.error("fail task:{} error,cause by:{}",task.getId(), e);
                }
            }
        }

    }

    /**
     * 完成子任务，如果父任务失败了，子任务不会执行
     * @param task
     * @param detail
     * @throws Exception
     */
    private void finish(Task task,TaskDetail detail) throws Exception {

        //查看是否有子类任务
        List<Task> childTasks = taskRepository.getChilds(task.getId());
        if(childTasks == null || childTasks.isEmpty()) {
            //当没有子任务时完成父任务
            taskRepository.finish(task,detail);
            return;
        } else {
            for (Task childTask : childTasks) {
                //开始任务
                TaskDetail childDetail = null;
                try {
                    //将子任务状态改成执行中
                    childTask.setStatus(TaskStatus.DOING);
                    childTask.setNodeId(config.getNodeId());
                    //开始子任务
                    childDetail = taskRepository.startChild(childTask,detail);
                    //使用乐观锁更新下状态，不然这里可能和恢复线程产生并发问题
                    int n = taskRepository.updateWithVersion(childTask);
                    if (n > 0) {
                        //再从数据库取一下，避免上面update修改后version不同步
                        childTask = taskRepository.get(childTask.getId());
                        //执行子任务
                        childTask.getInvokor().invoke();
                        //完成子任务
                        finish(childTask, childDetail);
                    }
                } catch (Exception e) {
                    logger.error("execute child task error,cause by:{}", e);
                    try {
                        taskRepository.fail(childTask, childDetail, e.getCause().getMessage());
                    } catch (Exception e1) {
                        logger.error("fail child task error,cause by:{}", e);
                    }
                }
            }
            /**
             * 当有子任务时完成子任务后再完成父任务
             */
            taskRepository.finish(task,detail);

        }

    }

    /**
     * 添加任务
     * @param name
     * @param cronExp
     * @param invockor
     * @return
     * @throws Exception
     */
    public long addTask(String name, String cronExp, Invocation invockor) throws Exception {
        Task task = new Task(name,cronExp,invockor);
        return taskRepository.insert(task);
    }

    /**
     * 添加子任务
     * @param pid
     * @param name
     * @param cronExp
     * @param invockor
     * @return
     * @throws Exception
     */
    public long addChildTask(Long pid,String name, String cronExp, Invocation invockor) throws Exception {
        Task task = new Task(name,cronExp,invockor);
        task.setPid(pid);
        return taskRepository.insert(task);
    }

}
