package com.rdpaas.task.handles;

import com.rdpaas.task.common.NotifyCmd;
import com.rdpaas.task.utils.SpringContextUtil;

/**
 * @author: rongdi
 * @date:
 */
public interface NotifyHandler<T> {

    static NotifyHandler chooseHandler(NotifyCmd notifyCmd) {
        return SpringContextUtil.getByTypeAndName(NotifyHandler.class,notifyCmd.toString());
    }

    public void update(T t);

}
