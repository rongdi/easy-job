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

    @Value("${easyjob.heartBeat.seconds:10000}")
    private int heartBeatSeconds;

    @Value("${easyjob.heartBeat.enable:true}")
    private boolean heartBeatEnable;

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
}
