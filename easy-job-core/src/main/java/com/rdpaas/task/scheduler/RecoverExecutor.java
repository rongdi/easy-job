package com.rdpaas.task.scheduler;

import com.rdpaas.task.common.Node;
import com.rdpaas.task.common.Task;
import com.rdpaas.task.common.TaskDetail;
import com.rdpaas.task.common.TaskStatus;
import com.rdpaas.task.config.EasyJobConfig;
import com.rdpaas.task.repository.NodeRepository;
import com.rdpaas.task.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 恢复调度器
 * @author rongdi
 * @date 2019-03-15 21:18
 */
@Component
public class RecoverExecutor {

    private static final Logger logger = LoggerFactory.getLogger(RecoverExecutor.class);

    @Autowired
    private EasyJobConfig config;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private NodeRepository nodeRepository;

    /**
     * 创建任务详情延时队列，用来做异常任务的重试
      */
    private DelayQueue<DelayItem<TaskDetail>> unRecoverQueue = new DelayQueue<>();

    /**
     * 直接使用系统自带工具
     */
    private ExecutorService bossPool = Executors.newFixedThreadPool(2);

    @PostConstruct
    public void init() {

    }

    class Boss implements Runnable {
        @Override
        public void run() {
            for (;;) {
                try {
                    /**
                     * 查找需要恢复的任务明细,这里界定需要恢复的任务是任务还没完成或者发生异常，并且所属执行节点超过3个
                     * 心跳周期没有更新心跳时间,并且没有超过重试次数的的明细记录
                     */
                    List<TaskDetail> details = taskRepository.listUnRecoverDetail(config.getHeartBeatSeconds() * 3,config.getRecoverMaxRetryCount());
                    handTaskDetail(details);
                    Thread.sleep(config.getRecoverSeconds() * 1000);
                } catch (Exception e) {
                    logger.error("Get next task failed,cause by:{}", e);
                }
            }
        }

    }

    /**
     * 处理需要恢复的任务明细，这里直接使用异常任务所属节点的下一个节点处理，降低多个
     * 恢复线程的竞争，避免无谓的锁竞争
     * @param details
     */
    private void handTaskDetail(List<TaskDetail> details) throws Exception {
        /**
         * 先获取可用的节点列表
         */
        List<Node> nodes = nodeRepository.getEnableNodes(config.getHeartBeatSeconds() * 2);
        if(nodes == null || nodes.isEmpty()) {
            return;
        }
        long maxNodeId = nodes.get(nodes.size() - 1).getNodeId();
        for(TaskDetail detail:details) {
            TaskStatus status = detail.getStatus();
            long nodeId = detail.getNodeId();
            /**
             * 从可用节点找到一个最靠近自己的节点
             */
            long currNodeId = chooseNodeId(nodes,maxNodeId,nodeId);
            long myNodeId = config.getNodeId();
            /**
             * 如果不该当前节点处理直接跳过
             */
            if(currNodeId != myNodeId) {
                continue;
            }
            /**
             * 这里使用乐观锁做一个保底，万一由于某些节点发生变化导致可用列表产生变化，这个任务明细
             * 也被其它线程认为是他的照成资源竞争的情况
             */
            detail.setNodeId(myNodeId);
            int n = taskRepository.updateWithVersion(detail);
            if(n > 0) {
                retryTaskDetail(detail);
            }
        }
    }

    /**
     * 重试任务
     * @param detail
     * @throws Exception
     */
    public void retryTaskDetail(TaskDetail detail) throws Exception {
        Task task = null;
        try {
            /**
             * 获取主任务对象，然后执行主任务,最后再重试子任务
             */
            task = taskRepository.get(detail.getTaskId());
            task.getInvokor().invoke();
        } catch (Exception e) {
            logger.error("retry task:{} error,cause by:{}",task == null ? 0 : task.getId(), e);
        }



    }

    /**
     * 重试子任务，如果父任务失败了，子任务不会执行
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
                    int n = taskRepository.update(childTask);
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
            //当有子任务时完成子任务后再完成父任务
            taskRepository.finish(task,detail);
        }

    }

    /**
     * 选择下一个节点
     * @param nodes
     * @param maxNodeId
     * @param nodeId
     * @return
     */
    private long chooseNodeId(List<Node> nodes,long maxNodeId,long nodeId) {
        if(nodeId > maxNodeId) {
            return nodes.get(0).getNodeId();
        }
        return nodes.stream().filter(node -> node.getNodeId() > nodeId).findFirst().get().getNodeId();
    }


}
