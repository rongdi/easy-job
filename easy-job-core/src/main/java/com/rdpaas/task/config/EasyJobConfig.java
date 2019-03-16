package com.rdpaas.task.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

@Component
public class EasyJobConfig {

    @Value("${easyjob.node.id:1}")
    private Long nodeId;
    
    /**
     * 节点取任务的策略
     */
    @Value("${easyjob.node.strategy:default}")
    private String nodeStrategy;

    /**
     * 节点取任务的周期，单位是毫秒，默认100毫秒
     */
    @Value("${easyjob.node.fetchPeriod:100}")
    private int fetchPeriod;
    
    /**
     * 节点取任务据当前的时间段，比如每次取还有5分钟开始的任务，这里单位是秒
     */
    @Value("${easyjob.node.fetchDuration:300}")
    private int fetchDuration;
    
    /**
     * 线程池中队列大小
     */
    @Value("${easyjob.pool.queueSize:1000}")
    private int queueSize;

    /**
     * 线程池中初始线程数量
     */
    @Value("${easyjob.pool.coreSize:5}")
    private int corePoolSize;

    /**
     * 线程池中最大线程数量
     */
    @Value("${easyjob.pool.maxSize:10}")
    private int maxPoolSize;

    /**
     * 节点心跳周期，单位秒
     */
    @Value("${easyjob.heartBeat.seconds:20}")
    private int heartBeatSeconds;

    /**
     * 节点心跳开关，默认开
     */
    @Value("${easyjob.heartBeat.enable:true}")
    private boolean heartBeatEnable;

    /**
     * 恢复线程开关，默认开
     */
    @Value("${easyjob.recover.enable:true}")
    private boolean recoverEnable;

    /**
     * 恢复线程周期，默认60s
     */
    @Value("${easyjob.recover.seconds:60}")
    private int recoverSeconds;

    @Bean(name = "easyjobDataSource")
    @Qualifier("easyjobDataSource")
    @ConfigurationProperties(prefix="easyjob.datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeStrategy() {
		return nodeStrategy;
	}

	public void setNodeStrategy(String nodeStrategy) {
		this.nodeStrategy = nodeStrategy;
	}

	public int getFetchPeriod() {
		return fetchPeriod;
	}

	public void setFetchPeriod(int fetchPeriod) {
		this.fetchPeriod = fetchPeriod;
	}

	public int getFetchDuration() {
		return fetchDuration;
	}

	public void setFetchDuration(int fetchDuration) {
		this.fetchDuration = fetchDuration;
	}

	public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public int getCorePoolSize() {
        return corePoolSize;
    }

    public void setCorePoolSize(int corePoolSize) {
        this.corePoolSize = corePoolSize;
    }

    public int getMaxPoolSize() {
        return maxPoolSize;
    }

    public void setMaxPoolSize(int maxPoolSize) {
        this.maxPoolSize = maxPoolSize;
    }

    public int getHeartBeatSeconds() {
        return heartBeatSeconds;
    }

    public void setHeartBeatSeconds(int heartBeatSeconds) {
        this.heartBeatSeconds = heartBeatSeconds;
    }

    public boolean isHeartBeatEnable() {
        return heartBeatEnable;
    }

    public void setHeartBeatEnable(boolean heartBeatEnable) {
        this.heartBeatEnable = heartBeatEnable;
    }

    public boolean isRecoverEnable() {
        return recoverEnable;
    }

    public void setRecoverEnable(boolean recoverEnable) {
        this.recoverEnable = recoverEnable;
    }

    public int getRecoverSeconds() {
        return recoverSeconds;
    }

    public void setRecoverSeconds(int recoverSeconds) {
        this.recoverSeconds = recoverSeconds;
    }

}
