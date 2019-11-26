package com.rdpaas.task.common;

import java.util.Date;

/**
 * 执行节点对象
 * @author rongdi
 * @date 2019-03-14 21:12
 */
public class Node {

    private Long id;

    /**
     * 节点ID，必须唯一
     */
    private Long nodeId;

    /**
     * 节点状态，0表示不可用，1表示可用
     */
    private NodeStatus status;

    /**
     * 节点序号
     */
    private Long rownum;

    /**
     * 执行任务次数
     */
    private Long counts;
    
    /**
     * 权重，默认都是1
     */
    private Integer weight = 1;

    /**
     * 通知指令
     */
    private NotifyCmd notifyCmd = NotifyCmd.NO_NOTIFY;

    /**
     * 通知值
     */
    private String notifyValue;

    /**
     * 节点创建时间
     */
    private Date createTime = new Date();

    /**
     * 更新时间
     */
    private Date updateTime = new Date();

    public Node(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Node() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public Long getRownum() {
        return rownum;
    }

    public void setRownum(Long rownum) {
        this.rownum = rownum;
    }

    public Long getCounts() {
        return counts;
    }

    public void setCounts(Long counts) {
        this.counts = counts;
    }

    public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
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

    public NodeStatus getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = NodeStatus.valueOf(status);
    }

    public NotifyCmd getNotifyCmd() {
        return notifyCmd;
    }

    public void setNotifyCmd(int notifyCmd) {
        this.notifyCmd = NotifyCmd.valueOf(notifyCmd);
    }

    public String getNotifyValue() {
        return notifyValue;
    }

    public void setNotifyValue(String notifyValue) {
        this.notifyValue = notifyValue;
    }
}
