package com.rdpaas.task.common;

import java.util.Date;

/**
 * 任务实体详情类
 * @author rongdi
 * @date 2019-03-12 19:03
 */
public class TaskDetail {

    private Long id;

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 所属父明细ID
     */
    private Long pid;

  /**
     * 当前执行的节点id
     */
    private Long nodeId;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 状态，0表示待执行，1表示执行中，2表示异常中，3表示已完成
     * 添加了任务明细说明就开始执行了
     */
    private TaskStatus status = TaskStatus.DOING;

    /**
     * 开始时间
     */
    private Date startTime = new Date();

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 乐观锁标识
     */
    private Integer version;

    /**
     * 错误信息
     */
    private String errorMsg;

    public TaskDetail() {
    }

    public TaskDetail(Long taskId) {
        this.taskId = taskId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
