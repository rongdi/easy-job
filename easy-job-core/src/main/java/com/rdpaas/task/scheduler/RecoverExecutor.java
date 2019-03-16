package com.rdpaas.task.scheduler;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rdpaas.task.common.Node;
import com.rdpaas.task.common.Task;
import com.rdpaas.task.common.TaskStatus;
import com.rdpaas.task.config.EasyJobConfig;
import com.rdpaas.task.repository.NodeRepository;
import com.rdpaas.task.repository.TaskRepository;

/**
 * 恢复调度器，恢复那些属于失联的节点的未完成的任务，异常也是一种任务状态的终点，
 * 由于不能确定异常是否只是偶发的，这里暂时不做恢复。这里可以这样恢复是因为几点
 * 1.子任务异常不影响主任务状态，只要主任务自己没有异常就不会是异常状态
 * 2.子任务未完成，主任务就算完成也是执行中的状态
 * 3.这里指的任务状态是task表的状态并不是task_detail的状态
 * @author rongdi
 * @date 2019-03-16 11:16
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


    @PostConstruct
    public void init() {
    	/**
    	 * 如果恢复线程开关是开着的，以守护线程方式启动恢复线程
    	 */
    	if(config.isRecoverEnable()) {
	    	Thread t = new Thread(new Recover());
	    	t.setDaemon(true);
	    	t.start();
    	}
    }

    class Recover implements Runnable {
        @Override
        public void run() {
            for (;;) {
                try {
                    /**
                     * 查找需要恢复的任务,这里界定需要恢复的任务是任务还没完成，并且所属执行节点超过3个
                     * 心跳周期没有更新心跳时间。由于这些任务由于当时执行节点没有来得及执行完就挂了，所以
                     * 只需要把状态再改回待执行，并且下次执行时间改成当前时间，让任务再次被调度一次
                     */
                    List<Task> tasks = taskRepository.listRecoverTasks(config.getHeartBeatSeconds() * 3);
                    if(tasks == null || tasks.isEmpty()) {
                    	return;
                    }
                   /**
                    * 先获取可用的节点列表
                    */
                   List<Node> nodes = nodeRepository.getEnableNodes(config.getHeartBeatSeconds() * 2);
                   if(nodes == null || nodes.isEmpty()) {
                       return;
                   }
                   long maxNodeId = nodes.get(nodes.size() - 1).getNodeId();
                    for (Task task : tasks) {
                        /**
                         * 每个节点有一个恢复线程，为了避免不必要的竞争,从可用节点找到一个最靠近任务所属节点的节点
                         */
                        long currNodeId = chooseNodeId(nodes,maxNodeId,task.getNodeId());
                        long myNodeId = config.getNodeId();
                        /**
                         * 如果不该当前节点处理直接跳过
                         */
                        if(currNodeId != myNodeId) {
                            continue;
                        }
                        /**
                         * 直接将任务状态改成待执行，并且节点改成当前节点
                         */
                    	task.setStatus(TaskStatus.PENDING);
                    	task.setNextStartTime(new Date());
                    	task.setNodeId(config.getNodeId());
                    	taskRepository.updateWithVersion(task);
                    }
                    Thread.sleep(config.getRecoverSeconds() * 1000);
                } catch (Exception e) {
                    logger.error("Get next task failed,cause by:{}", e);
                }
            }
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
