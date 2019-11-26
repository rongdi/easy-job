package com.rdpaas.task.common;

/**
 * @author rongdi
 * @date 2019/11/26
 */
public enum NotifyCmd {

    //没有通知，默认状态
    NO_NOTIFY(0),
    //开启具体任务(TaskDetail)
    START_TASK_DETAIL(1),
    //停止具体任务(TaskDetail)
    END_TASK_DETAIL(2),
    //开启任务(Task)
    START_TASK(3),
    //修改任务(Task)
    EDIT_TASK(4),
    //停止任务(Task)
    END_TASK(5);

    int id;

    NotifyCmd(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public  static NotifyCmd valueOf(int id) {
        switch (id) {
            case 1:
                return START_TASK_DETAIL;
            case 2:
                return END_TASK_DETAIL;
            case 3:
                return START_TASK;
            case 4:
                return EDIT_TASK;
            case 5:
                return END_TASK;
            default:
                return NO_NOTIFY;
        }
    }

}
