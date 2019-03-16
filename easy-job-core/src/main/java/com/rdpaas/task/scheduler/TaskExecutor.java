package com.rdpaas.task.scheduler;

import com.rdpaas.task.common.*;
import com.rdpaas.task.config.EasyJobConfig;
import com.rdpaas.task.repository.NodeRepository;
import com.rdpaas.task.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
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
     * 创建节点心跳延时队列
      */
    private DelayQueue<DelayItem<Node>> heartBeatQueue = new DelayQueue<>();

    /**
     * 可以明确知道最多只会运行两个线程，直接使用系统自带工具就可以了
     */
    private ExecutorService bossPool = Executors.newFixedThreadPool(2);

    private ThreadPoolExecutor workerPool;


    @PostConstruct
    public void init() {
        /**
         * 自定义线程池，初始线程数量corePoolSize，线程池等待队列大小queueSize，当初始线程都有任务，并且等待队列满后
         * 线程数量会自动扩充最大线程数maxSize，当新扩充的线程空闲60s后自动回收.自定义线程池是因为Executors那几个线程工具
         * 各有各的弊端，不适合生产使用
         */
        workerPool = new ThreadPoolExecutor(config.getCorePoolSize(), config.getMaxPoolSize(), 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(config.getQueueSize()));
        /**
         * 初始化一个节点到心跳队列，延时为0，用来注册节点
         */
        heartBeatQueue.offer(new DelayItem<>(0,new Node(config.getNodeId())));
        bossPool.execute(new Boss());
        /**
         * 如果心跳开关打开,假如心跳线程
         */
        if(config.isHeartBeatEnable()) {
            bossPool.execute(new HeartBeat());
        }
    }

    class Boss implements Runnable {
        @Override
        public void run() {
            for (;;) {
                try {
                    /**
                     * 查找待执行的任务
                     */
                    List<Task> tasks = taskRepository.listPedding(config.getQueueSize());
                    for(Task task:tasks) {
                        task.setStatus(TaskStatus.DOING);
                        task.setNodeId(config.getNodeId());
                        /**
                         * 使用乐观锁尝试更新状态，如果更新成功，其他节点就不会更新成功。如果在查询待执行任务列表
                         * 和当前这段时间有节点已经更新了这个任务，version必然和查出来时候的version不一样了,这里更新
                         * 必然会返回0了
                         */
                        int n = taskRepository.updateWithVersion(task);
                        if(n > 0) {
                            task = taskRepository.get(task.getId());
                            workerPool.execute(new Worker(task));
                        } else {
                            continue;
                        }
                    }
                    Thread.sleep(10);
                } catch (Exception e) {
                    logger.error("Get next task failed,cause by:{}", e);
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
     * 完成子任务，如果父任务失败了，子任务不会执行,子任务失败了，父任务状态也是失败的
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

    class HeartBeat implements Runnable {
        @Override
        public void run() {
            for(;;) {
                try {
                    /**
                     * 时间到了就可以从延时队列拿出节点对象，然后更新时间和序号，
                     * 最后再新建一个超时时间为心跳时间的节点对象放入延时队列，形成循环的心跳
                     */
                    DelayItem<Node> item = heartBeatQueue.take();
                    if(item != null && item.getItem() != null) {
                        Node node = item.getItem();
                        handHeartBeat(node);
                    }
                    heartBeatQueue.offer(new DelayItem<>(config.getHeartBeatSeconds() * 1000,new Node(config.getNodeId())));
                } catch (Exception e) {
                    logger.error("task heart beat error,cause by:{} ",e);
                }
            }
        }
    }

    /**
     * 处理节点心跳
     * @param node
     */
    private void handHeartBeat(Node node) {
        if(node == null) {
            return;
        }
        /**
         * 先看看数据库是否存在这个节点
         * 如果不存在：先查找下一个序号，然后设置到node对象中，最后插入
         * 如果存在：直接根据nodeId更新当前节点的序号和时间
         */
        Node currNode= nodeRepository.getByNodeId(node.getNodeId());
        if(currNode == null) {
            node.setRownum(nodeRepository.getNextRownum());
            nodeRepository.insert(node);
        } else  {
            nodeRepository.updateHeartBeat(node.getNodeId());
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

    public long addChildTask(Long pid,String name, String cronExp, Invocation invockor) throws Exception {
        Task task = new Task(name,cronExp,invockor);
        task.setPid(pid);
        return taskRepository.insert(task);
    }

}
