package com.rdpaas.task.common;

/**
 * @author rongdi
 * @date 2019/11/26
 */
public enum NotifyCmd {

    //没有通知，默认状态
    NO_NOTIFY(0),
    //开启任务(Task)
    START_TASK(1),
    //修改任务(Task)
    EDIT_TASK(2),
    //停止任务(Task)
    STOP_TASK(3);

    int id;

    NotifyCmd(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static NotifyCmd valueOf(int id) {
        switch (id) {
            case 1:
                return START_TASK;
            case 2:
                return EDIT_TASK;
            case 3:
                return STOP_TASK;
            default:
                return NO_NOTIFY;
        }
    }

}
