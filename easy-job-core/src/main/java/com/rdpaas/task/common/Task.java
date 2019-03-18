package com.rdpaas.task.common;

import java.util.Date;

/**
 * 任务实体
 * @author rongdi
 * @date 2019-03-12 19:02
 */
public class Task {

    private Long id;

    /**
     * 父任务id
     */
    private Long pid;

    /**
     * 调度名称
     */
    private String name;

    /**
     * cron表达式
     */
    private String cronExpr;

    /**
     * 当前执行的节点id
     */
    private Long nodeId;

    /**
     * 状态，0表示未开始，1表示待执行，2表示执行中，3表示已完成
     */
    private TaskStatus status = TaskStatus.NOT_STARTED;

    /**
     * 成功次数
     */
    private Integer successCount;

    /**
     * 失败次数
     */
    private Integer failCount;

    /**
     * 执行信息
     */
    private byte[] invokeInfo;

    /**
     * 乐观锁标识
     */
    private Integer version;

    /**
     * 首次开始时间
     */
    private Date firstStartTime;

    /**
     * 下次开始时间
     */
    private Date nextStartTime;

    /**
     * 创建时间
     */
    private Date createTime = new Date();

    /**
     * 更新时间
     */
    private Date updateTime = new Date();

    /**
     * 任务的执行者
     */
    private Invocation invokor;

    public Task() {
    }

    public Task(String name, String cronExpr, Invocation invokor) {
        this.name = name;
        this.cronExpr = cronExpr;
        this.invokor = invokor;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Date getFirstStartTime() {
        return firstStartTime;
    }

    public void setFirstStartTime(Date firstStartTime) {
        this.firstStartTime = firstStartTime;
    }

    public Date getNextStartTime() {
        return nextStartTime;
    }

    public void setNextStartTime(Date nextStartTime) {
        this.nextStartTime = nextStartTime;
    }

    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public byte[] getInvokeInfo() {
        return invokeInfo;
    }

    public void setInvokeInfo(byte[] invokeInfo) {
        this.invokeInfo = invokeInfo;
    }

    public Invocation getInvokor() {
        return invokor;
    }

    public void setInvokor(Invocation invokor) {
        this.invokor = invokor;
    }

    public Integer getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(Integer successCount) {
        this.successCount = successCount;
    }

    public Integer getFailCount() {
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }

    public String getCronExpr() {
        return cronExpr;
    }

    public void setCronExpr(String cronExpr) {
        this.cronExpr = cronExpr;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
