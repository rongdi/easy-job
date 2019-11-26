package com.rdpaas.task.common;

/**
 * 任务状态枚举类
 * @author rongdi
 * @date 2019-03-12 19:04
 */
public enum TaskStatus {

    //未开始
    NOT_STARTED(0),
    //待执行
    PENDING(1),
    //执行中
    DOING(2),
    //异常
    ERROR(3),
    //已完成
    FINISH(4),
    //已停止
    STOP(5);

    int id;

    TaskStatus(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public  static TaskStatus  valueOf(int id) {
        switch (id) {
            case 1:
                return PENDING;
            case 2:
                return DOING;
            case 3:
                return ERROR;
            case 4:
                return FINISH;
            case 5:
                return STOP;
            default:
                return NOT_STARTED;
        }
    }

}
