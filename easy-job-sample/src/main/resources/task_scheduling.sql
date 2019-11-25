CREATE DATABASE easy_job;

CREATE TABLE `easy_job_node` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `node_id` bigint(20) NOT NULL COMMENT '节点ID，必须唯一',
  `row_num` bigint(20) NOT NULL DEFAULT '0' COMMENT '节点序号',
  `counts` bigint(255) NOT NULL DEFAULT '0' COMMENT '执行次数',
  `weight` int(11) NOT NULL DEFAULT '1' COMMENT '权重',
  `status` int(11) NOT NULL DEFAULT '1' COMMENT '节点状态，1表示可用，0表示不可用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间，用于心跳更新',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_job_node_id` (`node_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `easy_job_task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  
  `pid` bigint(20) DEFAULT NULL COMMENT '任务父id,用于实现依赖任务，限制性父任务再执行子任务',
  `name` varchar(255) DEFAULT NULL COMMENT '调度名称',
  `cron_expr` varchar(255) DEFAULT NULL COMMENT 'cron表达式',
  `status` int(255) NOT NULL DEFAULT '0' COMMENT '状态，0表示待执行，1表示执行中，2表示异常中，3表示已完成',
  `fail_count` int(255) NOT NULL DEFAULT '0' COMMENT '失败执行次数',
  `success_count` int(255) NOT NULL DEFAULT '0' COMMENT '成功执行次数',
  `invoke_info` varbinary(10000) DEFAULT NULL COMMENT '序列化的执行类方法信息',
  `version` int(255) NOT NULL DEFAULT '0' COMMENT '乐观锁标识',
  `node_id` bigint(20) DEFAULT NULL COMMENT '当前执行节点id',
  `first_start_time` datetime DEFAULT NULL COMMENT '首次开始执行时间',
  `next_start_time` datetime DEFAULT NULL COMMENT '下次开始执行时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_tsk_next_stime` (`next_start_time`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE `easy_job_task_detail` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pid` bigint(20) DEFAULT NULL COMMENT '所属明细父ID',
  `task_id` bigint(20) NOT NULL COMMENT '所属任务ID',
  `node_id` bigint(20) NOT NULL COMMENT '执行节点id',
  `retry_count` int(8) NOT NULL DEFAULT 0 COMMENT '重试次数',

  `version` int(8) DEFAULT NULL COMMENT '乐观锁标识',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `status` int(8) NOT NULL DEFAULT '0' COMMENT '状态，0表示待执行，1表示执行中，2表示异常中，3表示已完成',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '失败原因',
  PRIMARY KEY (`id`),
  KEY `idx_tskd_task_id` (`task_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;