package com.rdpaas.task.handles;

import com.rdpaas.task.scheduler.TaskExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: rongdi
 * @date:
 */
@Component("STOP_TASK")
public class StopTaskHandler implements NotifyHandler<Long> {

    @Autowired
    private TaskExecutor taskExecutor;

    @Override
    public void update(Long taskId) {
        taskExecutor.stop(taskId);
    }

}
