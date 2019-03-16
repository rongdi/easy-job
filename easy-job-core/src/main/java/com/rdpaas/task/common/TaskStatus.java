package com.rdpaas.task.common;

/**
 * 任务状态枚举类
 * @author rongdi
 * @date 2019-03-12 19:04
 */
public enum TaskStatus {

    //待执行
    PENDING(0),
    //执行中
    DOING(1),
    //异常
    ERROR(2),
    //已完成
    FINISH(3);

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
                return DOING;
            case 2:
                return ERROR;
            case 3:
                return FINISH;
            default:
                return PENDING;
        }
    }

}
